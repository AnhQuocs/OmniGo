package com.example.omnigo.features.customer.ride.data.local

import com.example.omnigo.core.location.tracker.OsrmRoutingService
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetCustomerBookingsUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetNearbyDriversUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveDriverLiveLocationUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveRideBookingStatusUseCase
import com.example.omnigo.features.customer.ride.domain.util.BearingCalculator
import com.example.omnigo.features.customer.ride.domain.util.LiveTrackingCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideTrackingSessionManager @Inject constructor(
    private val observeRideBookingStatusUseCase: ObserveRideBookingStatusUseCase,
    private val observeDriverLiveLocationUseCase: ObserveDriverLiveLocationUseCase,
    private val getBookingDriverUseCase: GetBookingDriverUseCase,
    private val getCustomerBookingsUseCase: GetCustomerBookingsUseCase,
    private val getNearbyDriversUseCase: GetNearbyDriversUseCase,
    private val osrmRoutingService: OsrmRoutingService,
    private val rideSessionManager: RideSessionManager
) {
    internal var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    internal var pollingIntervalMs: Long = 4000L

    private val knownDriverLocations = java.util.concurrent.ConcurrentHashMap<Long, Pair<Double, Double>>()

    fun registerDriverLocation(driverId: Long, latitude: Double, longitude: Double) {
        if (driverId > 0L && latitude != 0.0 && longitude != 0.0) {
            knownDriverLocations[driverId] = Pair(latitude, longitude)
        }
    }

    private val _activeRideState = MutableStateFlow<ActiveRideState?>(null)
    val activeRideState: StateFlow<ActiveRideState?> = _activeRideState.asStateFlow()

    private var statusJob: Job? = null
    private var locationJob: Job? = null
    private var locationPollingJob: Job? = null
    private var routeJob: Job? = null
    private var currentDriverId: Long? = null
    private var lastRouteOriginLat: Double? = null
    private var lastRouteOriginLng: Double? = null
    private var lastRoutePhase: String? = null

    fun startSession(
        booking: Booking,
        pickupAddress: String = booking.pickupAddress,
        dropoffAddress: String = booking.dropoffAddress
    ) {
        if (booking.bookingId <= 0L) return

        stopTrackingRealtime()

        _activeRideState.value = ActiveRideState(
            bookingId = booking.bookingId,
            status = booking.status,
            startLatitude = booking.startLatitude,
            startLongitude = booking.startLongitude,
            endLatitude = booking.endLatitude,
            endLongitude = booking.endLongitude,
            pickupAddress = pickupAddress.ifBlank { booking.pickupAddress },
            dropoffAddress = dropoffAddress.ifBlank { booking.dropoffAddress },
            price = booking.price,
            paymentMethod = booking.paymentMethod,
            isSearchingDriver = booking.status == BookingStatus.PENDING
        )

        scope.launch {
            rideSessionManager.saveActiveBookingId(booking.bookingId)
        }

        observeStatus(booking.bookingId)

        val driverId = booking.driverId
        if (driverId != null && driverId > 0L) {
            subscribeDriverLocation(driverId)
            fetchDriverProfile(booking.bookingId)
        }
    }

    private fun observeStatus(bookingId: Long) {
        statusJob?.cancel()
        statusJob = scope.launch {
            observeRideBookingStatusUseCase(bookingId).collect { event ->
                handleStatusEvent(event)
            }
        }
    }

    fun handleStatusEvent(event: RideStatusEvent) {
        val current = _activeRideState.value ?: return
        if (event.bookingId != current.bookingId) return

        when (event.status) {
            BookingStatus.ACCEPTED -> {
                val assignedDriverId = event.driverId ?: current.driverProfile?.driverId
                lastRoutePhase = null
                _activeRideState.update { it?.copy(status = BookingStatus.ACCEPTED, isSearchingDriver = false, statusMessage = STATUS_ACCEPTED_MSG) }
                if (assignedDriverId != null && assignedDriverId > 0L) {
                    subscribeDriverLocation(assignedDriverId)
                    fetchDriverProfile(current.bookingId)
                }
            }
            BookingStatus.ARRIVED -> _activeRideState.update { it?.copy(status = BookingStatus.ARRIVED, isSearchingDriver = false, statusMessage = STATUS_ARRIVED_MSG) }
            BookingStatus.IN_PROGRESS -> {
                lastRoutePhase = null
                routeJob?.cancel()
                routeJob = null
                _activeRideState.update { it?.copy(status = BookingStatus.IN_PROGRESS, isSearchingDriver = false, routeCoordinates = emptyList(), statusMessage = STATUS_IN_PROGRESS_MSG) }
                _activeRideState.value?.driverLiveLocation?.let(::handleDriverLocation)
            }
            BookingStatus.PENDING -> {
                unsubscribeDriverLocation()
                routeJob?.cancel()
                routeJob = null
                lastRouteOriginLat = null
                lastRouteOriginLng = null
                lastRoutePhase = null
                _activeRideState.update {
                    it?.copy(
                        status = BookingStatus.PENDING, isSearchingDriver = true, driverProfile = null,
                        driverLiveLocation = null, liveDistanceKm = null, liveEtaMinutes = null,
                        routeCoordinates = emptyList(), statusMessage = STATUS_DRIVER_CANCELLED_MSG
                    )
                }
            }
            BookingStatus.COMPLETED, BookingStatus.CANCELLED -> {
                stopTrackingRealtime()
                scope.launch { rideSessionManager.clearActiveBookingId() }
                _activeRideState.update {
                    it?.copy(
                        status = event.status, isSearchingDriver = false, driverLiveLocation = null,
                        liveDistanceKm = null, liveEtaMinutes = null, routeCoordinates = emptyList(),
                        statusMessage = if (event.status == BookingStatus.COMPLETED) STATUS_COMPLETED_MSG else STATUS_CANCELLED_MSG
                    )
                }
            }
        }
    }

    private fun subscribeDriverLocation(driverId: Long) {
        if (driverId <= 0L || (currentDriverId == driverId && locationJob?.isActive == true && locationPollingJob?.isActive == true)) return

        unsubscribeDriverLocation()
        currentDriverId = driverId

        val known = knownDriverLocations[driverId]
        if (known != null && _activeRideState.value?.driverLiveLocation == null) {
            val cur = _activeRideState.value
            val inProg = cur?.status == BookingStatus.IN_PROGRESS
            val tLat = if (inProg) cur.endLatitude else (cur?.startLatitude ?: 0.0)
            val tLng = if (inProg) cur.endLongitude else (cur?.startLongitude ?: 0.0)
            val bearing = if (tLat != 0.0 && tLng != 0.0) BearingCalculator.calculateBearing(known.first, known.second, tLat, tLng) else null
            handleDriverLocation(DriverLiveLocation(driverId, known.first, known.second, bearing, 25.0f, System.currentTimeMillis()))
        }

        locationJob = scope.launch {
            observeDriverLiveLocationUseCase(driverId).collect(::handleDriverLocation)
        }

        locationPollingJob = scope.launch {
            fetchDriverLocationFromApi(driverId)
            if (pollingIntervalMs > 0L) {
                while (isActive) {
                    delay(pollingIntervalMs)
                    fetchDriverLocationFromApi(driverId)
                }
            }
        }
    }

    private fun unsubscribeDriverLocation() {
        locationJob?.cancel(); locationJob = null
        locationPollingJob?.cancel(); locationPollingJob = null
        currentDriverId = null
    }

    private suspend fun fetchDriverLocationFromApi(driverId: Long) {
        val current = _activeRideState.value ?: return
        if (!current.isTripActive) return

        val inProgress = current.status == BookingStatus.IN_PROGRESS
        val centerLat = current.driverLiveLocation?.latitude
            ?: (if (inProgress) current.endLatitude.takeIf { it != 0.0 } ?: current.startLatitude else current.startLatitude)
        val centerLng = current.driverLiveLocation?.longitude
            ?: (if (inProgress) current.endLongitude.takeIf { it != 0.0 } ?: current.startLongitude else current.startLongitude)
        if (centerLat == 0.0 || centerLng == 0.0) return

        try {
            val nearby = getNearbyDriversUseCase(latitude = centerLat, longitude = centerLng, radius = 50.0)
            val driver = nearby.firstOrNull { it.driverId == driverId }
            if (driver != null) {
                knownDriverLocations[driverId] = Pair(driver.latitude, driver.longitude)
                val targetLat = if (inProgress) current.endLatitude else current.startLatitude
                val targetLng = if (inProgress) current.endLongitude else current.startLongitude
                val bearing = if (targetLat != 0.0 && targetLng != 0.0) {
                    BearingCalculator.calculateBearing(driver.latitude, driver.longitude, targetLat, targetLng)
                } else null
                handleDriverLocation(DriverLiveLocation(driver.driverId, driver.latitude, driver.longitude, bearing, 25.0f, System.currentTimeMillis()))
                return
            }
        } catch (_: Exception) {}

        val targetLat = if (inProgress) current.endLatitude else current.startLatitude
        val targetLng = if (inProgress) current.endLongitude else current.startLongitude
        if (targetLat == 0.0 || targetLng == 0.0) return

        val currLoc = current.driverLiveLocation
        if (currLoc == null) {
            val known = knownDriverLocations[driverId]
            val initLat = known?.first ?: (targetLat + 0.0075)
            val initLng = known?.second ?: (targetLng - 0.0085)
            val bearing = BearingCalculator.calculateBearing(initLat, initLng, targetLat, targetLng)
            handleDriverLocation(DriverLiveLocation(driverId, initLat, initLng, bearing, 25.0f, System.currentTimeMillis()))
        } else if (current.routeCoordinates.size > 1) {
            val route = current.routeCoordinates
            val closestIdx = route.indices.minByOrNull {
                LiveTrackingCalculator.calculateRemainingDistanceKm(currLoc.latitude, currLoc.longitude, route[it].first, route[it].second)
            } ?: 0
            val next = route[(closestIdx + 2).coerceAtMost(route.size - 1)]
            val bearing = BearingCalculator.calculateBearing(currLoc.latitude, currLoc.longitude, next.first, next.second)
            handleDriverLocation(DriverLiveLocation(driverId, next.first, next.second, bearing, 25.0f, System.currentTimeMillis()))
        }
    }

    fun handleDriverLocation(liveLocation: DriverLiveLocation) {
        val current = _activeRideState.value ?: return
        if (liveLocation.driverId != currentDriverId && currentDriverId != null) return

        val inProgress = current.status == BookingStatus.IN_PROGRESS
        val targetLat = if (inProgress) current.endLatitude else current.startLatitude
        val targetLng = if (inProgress) current.endLongitude else current.startLongitude

        val (distKm, etaMins) = if (targetLat != 0.0 && targetLng != 0.0) {
            val d = LiveTrackingCalculator.calculateRemainingDistanceKm(
                fromLat = liveLocation.latitude,
                fromLng = liveLocation.longitude,
                toLat = targetLat,
                toLng = targetLng
            )
            val eta = LiveTrackingCalculator.calculateEtaMinutes(d, liveLocation.speed)
            Pair(d, eta)
        } else {
            Pair(null, null)
        }

        _activeRideState.update {
            it?.copy(
                driverLiveLocation = liveLocation,
                liveDistanceKm = distKm,
                liveEtaMinutes = etaMins
            )
        }

        val phase = when (current.status) {
            BookingStatus.ACCEPTED, BookingStatus.ARRIVED -> PHASE_TO_PICKUP
            BookingStatus.IN_PROGRESS -> PHASE_TO_DROPOFF
            else -> null
        }

        if (phase != null && targetLat != 0.0 && targetLng != 0.0) {
            fetchRouteIfNeeded(liveLocation.latitude, liveLocation.longitude, targetLat, targetLng, phase, current.driverProfile?.vehicleType ?: DEFAULT_VEHICLE_TYPE)
        }
    }

    private fun fetchRouteIfNeeded(
        driverLat: Double,
        driverLng: Double,
        targetLat: Double,
        targetLng: Double,
        phase: String,
        vehicleType: String
    ) {
        if (lastRoutePhase != phase) {
            routeJob?.cancel()
            routeJob = null
        } else if (routeJob?.isActive == true) {
            return
        }

        val lastLat = lastRouteOriginLat
        val lastLng = lastRouteOriginLng
        val shouldRefetch = lastRoutePhase != phase ||
            lastLat == null ||
            lastLng == null ||
            LiveTrackingCalculator.calculateRemainingDistanceKm(
                lastLat, lastLng, driverLat, driverLng
            ) >= ROUTE_REFETCH_THRESHOLD_KM

        if (!shouldRefetch) return

        lastRouteOriginLat = driverLat
        lastRouteOriginLng = driverLng
        lastRoutePhase = phase

        routeJob = scope.launch {
            val coords = osrmRoutingService.getRouteCoordinates(
                startLat = driverLat,
                startLng = driverLng,
                endLat = targetLat,
                endLng = targetLng,
                vehicleType = vehicleType
            )
            if (coords.isNotEmpty() && lastRoutePhase == phase) {
                _activeRideState.update { it?.copy(routeCoordinates = coords) }
            }
        }
    }

    private fun fetchDriverProfile(bookingId: Long) {
        scope.launch {
            _activeRideState.update { it?.copy(isDriverProfileLoading = true) }
            when (val result = getBookingDriverUseCase(bookingId)) {
                is GetBookingDriverResult.Success -> {
                    _activeRideState.update { it?.copy(driverProfile = result.driverProfile, isDriverProfileLoading = false) }
                    val currentLoc = _activeRideState.value?.driverLiveLocation
                    if (currentLoc != null && _activeRideState.value?.routeCoordinates.isNullOrEmpty()) handleDriverLocation(currentLoc)
                }
                is GetBookingDriverResult.Error -> _activeRideState.update { it?.copy(isDriverProfileLoading = false) }
            }
        }
    }

    suspend fun restoreSessionIfAvailable() {
        val savedBookingId = rideSessionManager.activeBookingIdFlow.firstOrNull() ?: 0L
        val current = _activeRideState.value
        if (savedBookingId > 0L && current?.bookingId == savedBookingId && (current.isTripActive || current.isPending)) return

        when (val result = getCustomerBookingsUseCase()) {
            is GetCustomerBookingsResult.Success -> {
                val activeBooking = (if (savedBookingId > 0L) result.bookings.firstOrNull { it.bookingId == savedBookingId && it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED } else null)
                    ?: result.bookings.firstOrNull { it.status == BookingStatus.PENDING || it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ARRIVED || it.status == BookingStatus.IN_PROGRESS }
                if (activeBooking != null) startSession(activeBooking)
                else if (savedBookingId > 0L || current != null) clearSession()
            }
            is GetCustomerBookingsResult.Error -> {}
        }
    }

    fun stopTrackingRealtime() {
        statusJob?.cancel(); statusJob = null
        routeJob?.cancel(); routeJob = null
        lastRouteOriginLat = null; lastRouteOriginLng = null; lastRoutePhase = null
        unsubscribeDriverLocation()
    }

    fun clearSession() {
        stopTrackingRealtime()
        _activeRideState.value = null
        knownDriverLocations.clear()
        scope.launch { rideSessionManager.clearActiveBookingId() }
    }

    companion object {
        const val STATUS_ACCEPTED_MSG = "STATUS_ACCEPTED"
        const val STATUS_ARRIVED_MSG = "STATUS_ARRIVED"
        const val STATUS_IN_PROGRESS_MSG = "STATUS_IN_PROGRESS"
        const val STATUS_COMPLETED_MSG = "STATUS_COMPLETED"
        const val STATUS_CANCELLED_MSG = "STATUS_CANCELLED"
        const val STATUS_DRIVER_CANCELLED_MSG = "STATUS_DRIVER_CANCELLED"

        private const val ROUTE_REFETCH_THRESHOLD_KM = 0.15
        private const val PHASE_TO_PICKUP = "TO_PICKUP"
        private const val PHASE_TO_DROPOFF = "TO_DROPOFF"
        private const val DEFAULT_VEHICLE_TYPE = "BIKE"
    }
}
