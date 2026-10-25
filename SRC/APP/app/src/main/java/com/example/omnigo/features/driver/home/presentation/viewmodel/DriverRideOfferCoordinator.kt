package com.example.omnigo.features.driver.home.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.AcceptRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import com.example.omnigo.features.driver.domain.usecase.AcceptRideBookingUseCase
import com.example.omnigo.features.driver.domain.usecase.ObserveRideOffersUseCase
import com.example.omnigo.features.driver.domain.usecase.RejectRideBookingUseCase
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

class DriverRideOfferCoordinator @Inject constructor(
    private val observeRideOffersUseCase: ObserveRideOffersUseCase,
    private val acceptRideBookingUseCase: AcceptRideBookingUseCase,
    private val rejectRideBookingUseCase: RejectRideBookingUseCase,
    private val driverTripSessionManager: DriverTripSessionManager? = null
) {
    private var offersJob: Job? = null
    private var countdownJob: Job? = null
    private var isProcessingOffer: Boolean = false
    internal var timeProvider: () -> Long = { System.currentTimeMillis() }

    fun startObserving(
        scope: CoroutineScope,
        updateState: ((DriverHomeUiState) -> DriverHomeUiState) -> Unit,
        getState: () -> DriverHomeUiState
    ) {
        offersJob?.cancel()
        offersJob = scope.launch {
            observeRideOffersUseCase().collect { offer ->
                handleIncomingOffer(scope, offer, updateState, getState)
            }
        }
    }

    fun stopObserving(updateState: ((DriverHomeUiState) -> DriverHomeUiState) -> Unit) {
        offersJob?.cancel()
        offersJob = null
        cancelCountdown()
        updateState {
            it.copy(
                currentRideOffer = null,
                rideOfferCountdownSeconds = RideOfferEvent.DEFAULT_TIMEOUT_SECONDS,
                rideOfferCountdownProgress = 1f,
                isRideOfferAccepting = false,
                isRideOfferRejecting = false
            )
        }
        isProcessingOffer = false
    }

    fun handleIncomingOffer(
        scope: CoroutineScope,
        offer: RideOfferEvent,
        updateState: ((DriverHomeUiState) -> DriverHomeUiState) -> Unit,
        getState: () -> DriverHomeUiState
    ) {
        val currentState = getState()
        if (!currentState.isOnline) return
        if (offer.bookingId <= 0L) return

        if (offer.status == BookingStatus.CANCELLED) {
            if (currentState.currentRideOffer?.bookingId == offer.bookingId) {
                cancelCountdown()
                isProcessingOffer = false
                updateState {
                    it.copy(
                        currentRideOffer = null,
                        offerActionMessage = CANCELLED_OFFER_MESSAGE
                    )
                }
            }
            return
        }

        if (currentState.isRideOfferAccepting || currentState.isRideOfferRejecting) return
        if (currentState.currentRideOffer?.bookingId == offer.bookingId) return

        cancelCountdown()
        isProcessingOffer = false

        updateState {
            it.copy(
                currentRideOffer = offer,
                rideOfferCountdownSeconds = offer.timeoutSeconds,
                rideOfferCountdownProgress = 1f,
                isRideOfferAccepting = false,
                isRideOfferRejecting = false
            )
        }
        startCountdown(scope, offer, updateState, getState)
    }

    private fun startCountdown(
        scope: CoroutineScope,
        offer: RideOfferEvent,
        updateState: ((DriverHomeUiState) -> DriverHomeUiState) -> Unit,
        getState: () -> DriverHomeUiState
    ) {
        cancelCountdown()
        val totalMs = offer.timeoutSeconds * 1000L
        val deadline = timeProvider() + totalMs

        countdownJob = scope.launch {
            while (isActive) {
                val now = timeProvider()
                val remainingMs = (deadline - now).coerceAtLeast(0L)
                val remainingSec = ceil(remainingMs / 1000.0).toInt().coerceAtLeast(0)
                val progress = if (totalMs > 0L) {
                    (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                updateState {
                    it.copy(
                        rideOfferCountdownSeconds = remainingSec,
                        rideOfferCountdownProgress = progress
                    )
                }

                if (remainingMs <= 0L) {
                    executeReject(scope, offer.bookingId, isTimeout = true, updateState, getState)
                    break
                }
                delay(1000L)
            }
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    fun onAcceptOffer(
        scope: CoroutineScope,
        bookingId: Long? = null,
        updateState: ((DriverHomeUiState) -> DriverHomeUiState) -> Unit,
        getState: () -> DriverHomeUiState
    ) {
        val currentState = getState()
        val targetId = bookingId ?: currentState.currentRideOffer?.bookingId ?: return
        if (isProcessingOffer || currentState.isRideOfferAccepting || currentState.isRideOfferRejecting) {
            return
        }
        val currentOffer = currentState.currentRideOffer
        if (currentOffer == null || currentOffer.bookingId != targetId) {
            return
        }

        isProcessingOffer = true
        cancelCountdown()

        scope.launch {
            updateState { it.copy(isRideOfferAccepting = true, errorMessage = null) }
            val result = acceptRideBookingUseCase(targetId)
            when (result) {
                is AcceptRideBookingResult.Success -> {
                    driverTripSessionManager?.saveActiveTrip(result.booking)
                    updateState {
                        it.copy(
                            currentRideOffer = null,
                            isRideOfferAccepting = false,
                            acceptedRideBookingId = targetId
                        )
                    }
                }
                is AcceptRideBookingResult.Error -> {
                    updateState {
                        it.copy(
                            currentRideOffer = null,
                            isRideOfferAccepting = false,
                            errorMessage = result.message ?: "Failed to accept ride booking"
                        )
                    }
                }
            }
            isProcessingOffer = false
        }
    }

    fun onRejectOffer(
        scope: CoroutineScope,
        bookingId: Long? = null,
        updateState: ((DriverHomeUiState) -> DriverHomeUiState) -> Unit,
        getState: () -> DriverHomeUiState
    ) {
        val currentState = getState()
        val targetId = bookingId ?: currentState.currentRideOffer?.bookingId ?: return
        executeReject(scope, targetId, isTimeout = false, updateState, getState)
    }

    private fun executeReject(
        scope: CoroutineScope,
        bookingId: Long,
        isTimeout: Boolean,
        updateState: ((DriverHomeUiState) -> DriverHomeUiState) -> Unit,
        getState: () -> DriverHomeUiState
    ) {
        val currentState = getState()
        if (isProcessingOffer || currentState.isRideOfferRejecting || currentState.isRideOfferAccepting) {
            return
        }
        val currentOffer = currentState.currentRideOffer
        if (currentOffer == null || currentOffer.bookingId != bookingId) {
            return
        }

        isProcessingOffer = true
        cancelCountdown()

        scope.launch {
            updateState { it.copy(isRideOfferRejecting = true) }
            val result = rejectRideBookingUseCase(bookingId)
            when (result) {
                is RejectRideBookingResult.Success -> {
                    updateState {
                        it.copy(
                            currentRideOffer = null,
                            isRideOfferRejecting = false,
                            offerActionMessage = if (isTimeout) TIMEOUT_OFFER_MESSAGE else null
                        )
                    }
                }
                is RejectRideBookingResult.Error -> {
                    updateState {
                        it.copy(
                            currentRideOffer = null,
                            isRideOfferRejecting = false,
                            errorMessage = result.message ?: "Failed to reject ride booking"
                        )
                    }
                }
            }
            isProcessingOffer = false
        }
    }

    companion object {
        const val TIMEOUT_OFFER_MESSAGE = "TIMEOUT_RIDE_OFFER_REJECTED"
        const val CANCELLED_OFFER_MESSAGE = "CANCELLED_RIDE_OFFER"
    }
}
