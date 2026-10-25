package com.example.omnigo.features.customer.ride.data.local

import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetCustomerBookingsUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveDriverLiveLocationUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveRideBookingStatusUseCase
import com.example.omnigo.features.customer.ride.domain.util.LiveTrackingCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideTrackingSessionManager @Inject constructor(
    private val observeRideBookingStatusUseCase: ObserveRideBookingStatusUseCase,
    private val observeDriverLiveLocationUseCase: ObserveDriverLiveLocationUseCase,
    private val getBookingDriverUseCase: GetBookingDriverUseCase,
    private val getCustomerBookingsUseCase: GetCustomerBookingsUseCase,
    private val rideSessionManager: RideSessionManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val _activeRideState = MutableStateFlow<ActiveRideState?>(null)
    val activeRideState: StateFlow<ActiveRideState?> = _activeRideState.asStateFlow()

    private var statusJob: Job? = null
    private var locationJob: Job? = null
    private var currentDriverId: Long? = null

    fun startSession(
        booking: Booking,
        pickupAddress: String = "",
        dropoffAddress: String = ""
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
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
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
                _activeRideState.update {
                    it?.copy(
                        status = BookingStatus.ACCEPTED,
                        isSearchingDriver = false,
                        statusMessage = STATUS_ACCEPTED_MSG
                    )
                }
                if (assignedDriverId != null && assignedDriverId > 0L) {
                    subscribeDriverLocation(assignedDriverId)
                    fetchDriverProfile(current.bookingId)
                }
            }

            BookingStatus.ARRIVED -> {
                _activeRideState.update {
                    it?.copy(
                        status = BookingStatus.ARRIVED,
                        isSearchingDriver = false,
                        statusMessage = STATUS_ARRIVED_MSG
                    )
                }
            }

            BookingStatus.IN_PROGRESS -> {
                _activeRideState.update {
                    it?.copy(
                        status = BookingStatus.IN_PROGRESS,
                        isSearchingDriver = false,
                        statusMessage = STATUS_IN_PROGRESS_MSG
                    )
                }
            }

            BookingStatus.PENDING -> {
                // Driver cancelled -> Revert to PENDING to auto-find new driver
                unsubscribeDriverLocation()
                _activeRideState.update {
                    it?.copy(
                        status = BookingStatus.PENDING,
                        isSearchingDriver = true,
                        driverProfile = null,
                        driverLiveLocation = null,
                        liveDistanceKm = null,
                        liveEtaMinutes = null,
                        statusMessage = STATUS_DRIVER_CANCELLED_MSG
                    )
                }
            }

            BookingStatus.COMPLETED,
            BookingStatus.CANCELLED -> {
                stopTrackingRealtime()
                scope.launch {
                    rideSessionManager.clearActiveBookingId()
                }
                _activeRideState.update {
                    it?.copy(
                        status = event.status,
                        isSearchingDriver = false,
                        driverLiveLocation = null,
                        liveDistanceKm = null,
                        liveEtaMinutes = null,
                        statusMessage = if (event.status == BookingStatus.COMPLETED) {
                            STATUS_COMPLETED_MSG
                        } else {
                            STATUS_CANCELLED_MSG
                        }
                    )
                }
            }
        }
    }

    private fun subscribeDriverLocation(driverId: Long) {
        if (driverId <= 0L) return
        if (currentDriverId == driverId && locationJob?.isActive == true) return

        locationJob?.cancel()
        currentDriverId = driverId

        locationJob = scope.launch {
            observeDriverLiveLocationUseCase(driverId).collect { liveLocation ->
                handleDriverLocation(liveLocation)
            }
        }
    }

    private fun unsubscribeDriverLocation() {
        locationJob?.cancel()
        locationJob = null
        currentDriverId = null
    }

    fun handleDriverLocation(liveLocation: DriverLiveLocation) {
        val current = _activeRideState.value ?: return
        if (liveLocation.driverId != currentDriverId && currentDriverId != null) return

        val targetLat = if (current.status == BookingStatus.IN_PROGRESS) {
            current.endLatitude
        } else {
            current.startLatitude
        }

        val targetLng = if (current.status == BookingStatus.IN_PROGRESS) {
            current.endLongitude
        } else {
            current.startLongitude
        }

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
    }

    private fun fetchDriverProfile(bookingId: Long) {
        scope.launch {
            _activeRideState.update { it?.copy(isDriverProfileLoading = true) }
            when (val result = getBookingDriverUseCase(bookingId)) {
                is GetBookingDriverResult.Success -> {
                    _activeRideState.update {
                        it?.copy(
                            driverProfile = result.driverProfile,
                            isDriverProfileLoading = false
                        )
                    }
                }
                is GetBookingDriverResult.Error -> {
                    _activeRideState.update {
                        it?.copy(isDriverProfileLoading = false)
                    }
                }
            }
        }
    }

    suspend fun restoreSessionIfAvailable() {
        val savedBookingId = rideSessionManager.activeBookingIdFlow.firstOrNull() ?: return
        if (savedBookingId <= 0L) return

        // Already active in memory for this bookingId
        if (_activeRideState.value?.bookingId == savedBookingId && _activeRideState.value?.isTripActive == true) {
            return
        }

        when (val result = getCustomerBookingsUseCase()) {
            is GetCustomerBookingsResult.Success -> {
                val matching = result.bookings.firstOrNull { it.bookingId == savedBookingId }
                if (matching != null) {
                    if (matching.status == BookingStatus.COMPLETED || matching.status == BookingStatus.CANCELLED) {
                        clearSession()
                    } else {
                        startSession(matching)
                    }
                } else {
                    clearSession()
                }
            }
            is GetCustomerBookingsResult.Error -> {
                // Network unavailable; keep existing state if any, otherwise await network recovery
            }
        }
    }

    fun stopTrackingRealtime() {
        statusJob?.cancel()
        statusJob = null
        unsubscribeDriverLocation()
    }

    fun clearSession() {
        stopTrackingRealtime()
        _activeRideState.value = null
        scope.launch {
            rideSessionManager.clearActiveBookingId()
        }
    }

    companion object {
        const val STATUS_ACCEPTED_MSG = "STATUS_ACCEPTED"
        const val STATUS_ARRIVED_MSG = "STATUS_ARRIVED"
        const val STATUS_IN_PROGRESS_MSG = "STATUS_IN_PROGRESS"
        const val STATUS_COMPLETED_MSG = "STATUS_COMPLETED"
        const val STATUS_CANCELLED_MSG = "STATUS_CANCELLED"
        const val STATUS_DRIVER_CANCELLED_MSG = "STATUS_DRIVER_CANCELLED"
    }
}
