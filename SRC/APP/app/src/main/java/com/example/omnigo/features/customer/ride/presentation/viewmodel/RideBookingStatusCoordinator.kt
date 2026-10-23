package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.features.customer.ride.data.local.RideSessionManager
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveRideBookingStatusUseCase
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class RideBookingStatusCoordinator @Inject constructor(
    private val observeRideBookingStatusUseCase: ObserveRideBookingStatusUseCase,
    private val getBookingDriverUseCase: GetBookingDriverUseCase,
    private val rideSessionManager: RideSessionManager
) {
    private var statusJob: Job? = null

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
                updateState {
                    it.copy(
                        isSearchingDriver = false,
                        currentBooking = it.currentBooking?.copy(
                            status = BookingStatus.ACCEPTED,
                            driverId = event.driverId ?: it.currentBooking.driverId
                        ),
                        tripStatusMessage = STATUS_ACCEPTED_MSG
                    )
                }
                fetchDriverProfile(scope, event.bookingId, updateState)
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
                updateState {
                    it.copy(
                        isSearchingDriver = false,
                        currentBooking = null,
                        assignedDriver = null,
                        infoMessage = STATUS_CANCELLED_MSG,
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

    fun fetchDriverProfile(
        scope: CoroutineScope,
        bookingId: Long,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
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
                // Fetch driver and start observing
                fetchDriverProfile(scope, savedBookingId, updateState)
                startObserving(scope, savedBookingId, updateState, getState)
            }
        }
    }

    fun stopObserving() {
        statusJob?.cancel()
        statusJob = null
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
