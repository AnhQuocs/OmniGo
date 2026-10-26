package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.features.customer.ride.data.local.RideSessionManager
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveDriverLiveLocationUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveRideBookingStatusUseCase
import com.example.omnigo.features.customer.ride.domain.util.BearingCalculator
import com.example.omnigo.features.customer.ride.domain.util.LiveTrackingCalculator
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import com.example.omnigo.R
import com.example.omnigo.utils.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class RideBookingStatusCoordinator @Inject constructor(
    private val observeRideBookingStatusUseCase: ObserveRideBookingStatusUseCase,
    private val getBookingDriverUseCase: GetBookingDriverUseCase,
    private val observeDriverLiveLocationUseCase: ObserveDriverLiveLocationUseCase,
    private val rideSessionManager: RideSessionManager
) {
    private var statusJob: Job? = null
    private var locationJob: Job? = null

    fun startObserving(
        scope: CoroutineScope,
        bookingId: Long,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit,
        getState: () -> RideBookingUiState
    ) {
        if (bookingId <= 0L) return
        statusJob?.cancel()

        scope.launch {
            rideSessionManager.saveActiveBookingId(bookingId)
        }

        statusJob = scope.launch {
            observeRideBookingStatusUseCase(bookingId).collect { event ->
                handleStatusEvent(scope, event, updateState, getState)
            }
        }
    }

    fun handleStatusEvent(
        scope: CoroutineScope,
        event: RideStatusEvent,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit,
        getState: () -> RideBookingUiState
    ) {
        val currentState = getState()
        val currentBookingId = currentState.currentBooking?.bookingId ?: event.bookingId
        if (event.bookingId != currentBookingId) return

        when (event.status) {
            BookingStatus.ACCEPTED -> {
                val assignedDriverId = event.driverId ?: currentState.currentBooking?.driverId
                updateState {
                    it.copy(
                        isSearchingDriver = false,
                        currentBooking = it.currentBooking?.copy(
                            status = BookingStatus.ACCEPTED,
                            driverId = assignedDriverId ?: it.currentBooking.driverId
                        ),
                        tripStatusMessage = STATUS_ACCEPTED_MSG
                    )
                }
                fetchDriverProfile(scope, event.bookingId, updateState, getState)
                if (assignedDriverId != null && assignedDriverId > 0L) {
                    startObservingDriverLocation(scope, assignedDriverId, updateState, getState)
                }
            }
            BookingStatus.ARRIVED -> {
                updateState {
                    it.copy(
                        currentBooking = it.currentBooking?.copy(status = BookingStatus.ARRIVED),
                        tripStatusMessage = STATUS_ARRIVED_MSG
                    )
                }
            }
            BookingStatus.IN_PROGRESS -> {
                updateState {
                    it.copy(
                        currentBooking = it.currentBooking?.copy(status = BookingStatus.IN_PROGRESS),
                        tripStatusMessage = STATUS_IN_PROGRESS_MSG
                    )
                }
            }
            BookingStatus.COMPLETED -> {
                stopObservingDriverLocation(updateState)
                updateState {
                    it.copy(
                        isSearchingDriver = false,
                        currentBooking = it.currentBooking?.copy(
                            status = BookingStatus.COMPLETED,
                            price = event.price ?: it.currentBooking.price
                        ),
                        tripStatusMessage = STATUS_COMPLETED_MSG
                    )
                }
                scope.launch {
                    rideSessionManager.clearActiveBookingId()
                }
            }
            BookingStatus.CANCELLED -> {
                stopObservingDriverLocation(updateState)
                updateState {
                    it.copy(
                        isSearchingDriver = false,
                        currentBooking = null,
                        assignedDriver = null,
                        infoMessage = UiText.StringResource(R.string.ride_booking_cancelled_success),
                        tripStatusMessage = null
                    )
                }
                scope.launch {
                    rideSessionManager.clearActiveBookingId()
                }
            }
            BookingStatus.PENDING -> {
                updateState {
                    it.copy(
                        isSearchingDriver = true,
                        currentBooking = it.currentBooking?.copy(status = BookingStatus.PENDING)
                    )
                }
            }
        }
    }

    fun startObservingDriverLocation(
        scope: CoroutineScope,
        driverId: Long,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit,
        getState: () -> RideBookingUiState
    ) {
        if (driverId <= 0L) return
        locationJob?.cancel()
        locationJob = scope.launch {
            observeDriverLiveLocationUseCase(driverId).collect { liveLocation ->
                val state = getState()
                val booking = state.currentBooking
                val targetLat = if (booking?.status == BookingStatus.IN_PROGRESS) {
                    booking.endLatitude
                } else {
                    booking?.startLatitude
                }
                val targetLng = if (booking?.status == BookingStatus.IN_PROGRESS) {
                    booking.endLongitude
                } else {
                    booking?.startLongitude
                }

                val (distKm, etaMins) = if (targetLat != null && targetLng != null && targetLat != 0.0 && targetLng != 0.0) {
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

                val effectiveBearing = liveLocation.bearing ?: if (targetLat != null && targetLng != null && targetLat != 0.0 && targetLng != 0.0) {
                    BearingCalculator.calculateBearing(
                        lat1 = liveLocation.latitude,
                        lon1 = liveLocation.longitude,
                        lat2 = targetLat,
                        lon2 = targetLng
                    )
                } else null

                val processedLocation = liveLocation.copy(bearing = effectiveBearing)

                updateState {
                    it.copy(
                        driverLiveLocation = processedLocation,
                        liveDistanceKm = distKm,
                        liveEtaMinutes = etaMins
                    )
                }
            }
        }
    }

    fun stopObservingDriverLocation(
        updateState: (((RideBookingUiState) -> RideBookingUiState) -> Unit)? = null
    ) {
        locationJob?.cancel()
        locationJob = null
        updateState?.invoke {
            it.copy(
                driverLiveLocation = null,
                liveDistanceKm = null,
                liveEtaMinutes = null
            )
        }
    }

    fun fetchDriverProfile(
        scope: CoroutineScope,
        bookingId: Long,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit,
        getState: () -> RideBookingUiState
    ) {
        scope.launch {
            updateState { it.copy(isDriverProfileLoading = true) }
            val result = getBookingDriverUseCase(bookingId)
            when (result) {
                is GetBookingDriverResult.Success -> {
                    updateState {
                        it.copy(
                            assignedDriver = result.driverProfile,
                            isDriverProfileLoading = false
                        )
                    }
                    startObservingDriverLocation(scope, result.driverProfile.driverId, updateState, getState)
                }
                is GetBookingDriverResult.Error -> {
                    updateState {
                        it.copy(isDriverProfileLoading = false)
                    }
                }
            }
        }
    }

    fun restoreSessionIfAvailable(
        scope: CoroutineScope,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit,
        getState: () -> RideBookingUiState
    ) {
        scope.launch {
            val savedBookingId = rideSessionManager.activeBookingIdFlow.firstOrNull() ?: return@launch
            if (savedBookingId > 0L && getState().currentBooking == null) {
                fetchDriverProfile(scope, savedBookingId, updateState, getState)
                startObserving(scope, savedBookingId, updateState, getState)
            }
        }
    }

    fun stopObserving() {
        statusJob?.cancel()
        statusJob = null
        locationJob?.cancel()
        locationJob = null
    }

    suspend fun clearSession() {
        rideSessionManager.clearActiveBookingId()
    }

    companion object {
        const val STATUS_ACCEPTED_MSG = "STATUS_ACCEPTED"
        const val STATUS_ARRIVED_MSG = "STATUS_ARRIVED"
        const val STATUS_IN_PROGRESS_MSG = "STATUS_IN_PROGRESS"
        const val STATUS_COMPLETED_MSG = "STATUS_COMPLETED"
        const val STATUS_CANCELLED_MSG = "STATUS_CANCELLED"
    }
}
