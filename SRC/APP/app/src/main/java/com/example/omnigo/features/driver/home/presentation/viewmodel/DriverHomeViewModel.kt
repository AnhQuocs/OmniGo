package com.example.omnigo.features.driver.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.features.driver.domain.usecase.AcceptFoodOrderUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.ObserveDriverFoodOffersUseCase
import com.example.omnigo.features.driver.domain.usecase.RejectFoodOrderUseCase
import com.example.omnigo.features.driver.domain.usecase.UpdateDriverStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class DriverHomeViewModel @Inject constructor(
    private val getDriverProfileUseCase: GetDriverProfileUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val locationTracker: LocationTracker,
    private val sessionManager: SessionManager,
    private val observeDriverFoodOffersUseCase: ObserveDriverFoodOffersUseCase,
    private val acceptFoodOrderUseCase: AcceptFoodOrderUseCase,
    private val rejectFoodOrderUseCase: RejectFoodOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState.asStateFlow()

    private var offersJob: Job? = null
    private var countdownJob: Job? = null
    private var isProcessingOffer: Boolean = false
    internal var timeProvider: () -> Long = { System.currentTimeMillis() }

    init {
        loadDriverProfile()
        checkLocationPrerequisites()
    }

    fun loadDriverProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = sessionManager.getUserId()
            val result = getDriverProfileUseCase(userId)
            result.onSuccess { profile ->
                val online = profile.isOnline
                _uiState.update {
                    it.copy(
                        profile = profile,
                        approvalStatus = profile.approvalStatus,
                        isOnline = online,
                        isLoading = false
                    )
                }
                if (online) {
                    startObservingOffers()
                } else {
                    stopObservingOffers()
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }

    fun checkLocationPrerequisites() {
        val hasPermission = locationTracker.isLocationPermissionGranted()
        val isGpsOn = locationTracker.isGpsEnabled()
        _uiState.update {
            it.copy(
                isLocationPermissionGranted = hasPermission,
                isGpsEnabled = isGpsOn
            )
        }
    }

    fun toggleOnlineStatus(targetOnline: Boolean) {
        if (targetOnline) {
            val currentApproval = _uiState.value.approvalStatus
            if (currentApproval != DriverApprovalStatus.APPROVED) {
                _uiState.update { it.copy(showApprovalWarningDialog = true) }
                return
            }

            checkLocationPrerequisites()
            if (!_uiState.value.isLocationPermissionGranted) {
                _uiState.update { it.copy(errorMessage = ERROR_LOCATION_PERMISSION_REQUIRED) }
                return
            }

            if (!_uiState.value.isGpsEnabled) {
                _uiState.update { it.copy(errorMessage = ERROR_GPS_REQUIRED) }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isStatusUpdating = true, errorMessage = null) }
            val driverId = _uiState.value.profile?.driverId ?: sessionManager.getUserId()
            val result = updateDriverStatusUseCase(driverId, targetOnline)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isOnline = targetOnline,
                        isStatusUpdating = false
                    )
                }
                if (targetOnline) {
                    startObservingOffers()
                } else {
                    stopObservingOffers()
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isStatusUpdating = false,
                        errorMessage = error.message ?: "Failed to update driver status"
                    )
                }
            }
        }
    }

    fun startObservingOffers() {
        offersJob?.cancel()
        offersJob = viewModelScope.launch {
            observeDriverFoodOffersUseCase().collect { offer ->
                handleIncomingOffer(offer)
            }
        }
    }

    fun stopObservingOffers() {
        offersJob?.cancel()
        offersJob = null
        cancelCountdown()
        _uiState.update {
            it.copy(
                currentFoodOffer = null,
                offerCountdownSeconds = DriverFoodOffer.DEFAULT_TIMEOUT_SECONDS,
                offerCountdownProgress = 1f,
                isOfferAccepting = false,
                isOfferRejecting = false
            )
        }
        isProcessingOffer = false
    }

    fun handleIncomingOffer(offer: DriverFoodOffer) {
        if (!_uiState.value.isOnline) return
        if (offer.orderId <= 0L) return
        if (_uiState.value.isOfferAccepting) return
        if (_uiState.value.currentFoodOffer?.orderId == offer.orderId) return

        cancelCountdown()
        isProcessingOffer = false

        _uiState.update {
            it.copy(
                currentFoodOffer = offer,
                offerCountdownSeconds = offer.timeoutSeconds,
                offerCountdownProgress = 1f,
                isOfferAccepting = false,
                isOfferRejecting = false
            )
        }
        startCountdown(offer)
    }

    private fun startCountdown(offer: DriverFoodOffer) {
        cancelCountdown()
        val totalMs = offer.timeoutSeconds * 1000L
        val deadline = timeProvider() + totalMs

        countdownJob = viewModelScope.launch {
            while (isActive) {
                val now = timeProvider()
                val remainingMs = (deadline - now).coerceAtLeast(0L)
                val remainingSec = ceil(remainingMs / 1000.0).toInt().coerceAtLeast(0)
                val progress = if (totalMs > 0L) {
                    (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                _uiState.update {
                    it.copy(
                        offerCountdownSeconds = remainingSec,
                        offerCountdownProgress = progress
                    )
                }

                if (remainingMs <= 0L) {
                    executeReject(offer.orderId, isTimeout = true)
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    fun onAcceptOffer(orderId: Long? = null) {
        val targetOrderId = orderId ?: _uiState.value.currentFoodOffer?.orderId ?: return
        if (isProcessingOffer || _uiState.value.isOfferAccepting || _uiState.value.isOfferRejecting) {
            return
        }
        val currentOffer = _uiState.value.currentFoodOffer
        if (currentOffer == null || currentOffer.orderId != targetOrderId) {
            return
        }

        isProcessingOffer = true
        cancelCountdown()

        viewModelScope.launch {
            _uiState.update { it.copy(isOfferAccepting = true, errorMessage = null) }
            val result = acceptFoodOrderUseCase(targetOrderId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        currentFoodOffer = null,
                        isOfferAccepting = false,
                        acceptedOrderId = targetOrderId
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        currentFoodOffer = null,
                        isOfferAccepting = false,
                        errorMessage = error.message ?: "Failed to accept food order"
                    )
                }
            }
            isProcessingOffer = false
        }
    }

    fun onRejectOffer(orderId: Long? = null) {
        val targetOrderId = orderId ?: _uiState.value.currentFoodOffer?.orderId ?: return
        executeReject(targetOrderId, isTimeout = false)
    }

    private fun executeReject(orderId: Long, isTimeout: Boolean) {
        if (isProcessingOffer || _uiState.value.isOfferRejecting || _uiState.value.isOfferAccepting) {
            return
        }
        val currentOffer = _uiState.value.currentFoodOffer
        if (currentOffer == null || currentOffer.orderId != orderId) {
            return
        }

        isProcessingOffer = true
        cancelCountdown()

        viewModelScope.launch {
            _uiState.update { it.copy(isOfferRejecting = true) }
            val result = rejectFoodOrderUseCase(orderId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        currentFoodOffer = null,
                        isOfferRejecting = false,
                        offerActionMessage = if (isTimeout) TIMEOUT_MESSAGE else null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        currentFoodOffer = null,
                        isOfferRejecting = false,
                        errorMessage = error.message ?: "Failed to reject food order"
                    )
                }
            }
            isProcessingOffer = false
        }
    }

    fun dismissApprovalWarningDialog() {
        _uiState.update { it.copy(showApprovalWarningDialog = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearOfferActionMessage() {
        _uiState.update { it.copy(offerActionMessage = null) }
    }

    fun clearAcceptedOrderId() {
        _uiState.update { it.copy(acceptedOrderId = null) }
    }

    override fun onCleared() {
        super.onCleared()
        offersJob?.cancel()
        countdownJob?.cancel()
    }

    companion object {
        const val ERROR_LOCATION_PERMISSION_REQUIRED = "ERROR_LOCATION_PERMISSION_REQUIRED"
        const val ERROR_GPS_REQUIRED = "ERROR_GPS_REQUIRED"
        const val TIMEOUT_MESSAGE = "TIMEOUT_OFFER_REJECTED"
    }
}

