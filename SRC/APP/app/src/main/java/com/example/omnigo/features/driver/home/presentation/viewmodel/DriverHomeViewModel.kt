package com.example.omnigo.features.driver.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.features.driver.domain.usecase.AcceptFoodOrderUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.ObserveDriverFoodOffersUseCase
import com.example.omnigo.features.driver.domain.usecase.RejectFoodOrderUseCase
import com.example.omnigo.features.driver.domain.usecase.UpdateDriverStatusUseCase
import com.example.omnigo.features.driver.food.domain.usecase.GetDriverActiveFoodOrdersUseCase
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
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
    private val rejectFoodOrderUseCase: RejectFoodOrderUseCase,
    private val getDriverActiveFoodOrdersUseCase: GetDriverActiveFoodOrdersUseCase,
    private val rideOfferCoordinator: DriverRideOfferCoordinator
) : ViewModel() {

    private var getDriverDailyReportUseCase: GetDriverDailyReportUseCase? = null
    private var driverTripSessionManager: DriverTripSessionManager? = null

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState.asStateFlow()

    private var offersJob: Job? = null
    private var countdownJob: Job? = null
    private var isProcessingOffer: Boolean = false
    internal var timeProvider: () -> Long = { System.currentTimeMillis() }

    init {
        loadDriverProfile()
        checkLocationPrerequisites()
        refreshDriverLocation()
    }

    @Inject
    fun injectCockpitDependencies(
        dailyReportUseCase: GetDriverDailyReportUseCase,
        tripSessionManager: DriverTripSessionManager
    ) {
        this.getDriverDailyReportUseCase = dailyReportUseCase
        this.driverTripSessionManager = tripSessionManager
        loadDailyReport()
        observeActiveRideSession()
    }

    private fun observeActiveRideSession() {
        val sessionManager = driverTripSessionManager ?: return
        viewModelScope.launch {
            val wasRestored = sessionManager.restoreActiveTrip()
            val restored = sessionManager.activeBooking.value
            if (wasRestored && restored?.status == BookingStatus.IN_PROGRESS) {
                _uiState.update { it.copy(acceptedRideBookingId = restored.bookingId) }
            }
            sessionManager.activeBooking.collect { booking ->
                _uiState.update { it.copy(activeRideBooking = booking) }
            }
        }
    }

    fun loadDailyReport(forceRefresh: Boolean = false) {
        val useCase = getDriverDailyReportUseCase ?: return
        viewModelScope.launch {
            when (val result = useCase(forceRefresh)) {
                is GetDriverDailyReportResult.Success -> _uiState.update { it.copy(dailyReport = result.report) }
                is GetDriverDailyReportResult.Error -> Unit
            }
        }
    }

    fun refreshDriverLocation() {
        viewModelScope.launch {
            if (locationTracker.isLocationPermissionGranted() && locationTracker.isGpsEnabled()) {
                val loc = runCatching { locationTracker.getCurrentLocation() }.getOrNull()
                if (loc != null) {
                    _uiState.update {
                        it.copy(currentLatitude = loc.latitude, currentLongitude = loc.longitude)
                    }
                }
            }
        }
    }

    fun loadDriverProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = sessionManager.getUserId()
            getDriverProfileUseCase(userId).onSuccess { profile ->
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
                    checkActiveDeliveries()
                } else {
                    stopObservingOffers()
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun checkLocationPrerequisites() {
        _uiState.update {
            it.copy(
                isLocationPermissionGranted = locationTracker.isLocationPermissionGranted(),
                isGpsEnabled = locationTracker.isGpsEnabled()
            )
        }
    }

    fun toggleOnlineStatus(targetOnline: Boolean) {
        if (targetOnline) {
            if (_uiState.value.approvalStatus != DriverApprovalStatus.APPROVED) {
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
            updateDriverStatusUseCase(driverId, targetOnline).onSuccess {
                _uiState.update { it.copy(isOnline = targetOnline, isStatusUpdating = false) }
                if (targetOnline) {
                    refreshDriverLocation()
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
            observeDriverFoodOffersUseCase().collect { offer -> handleIncomingOffer(offer) }
        }
        rideOfferCoordinator.startObserving(
            scope = viewModelScope,
            updateState = { _uiState.update(it) },
            getState = { _uiState.value }
        )
    }

    fun stopObservingOffers() {
        offersJob?.cancel()
        offersJob = null
        cancelCountdown()
        rideOfferCoordinator.stopObserving { _uiState.update(it) }
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
        if (!_uiState.value.isOnline || offer.orderId <= 0L || _uiState.value.isOfferAccepting) return
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
                val remainingMs = (deadline - timeProvider()).coerceAtLeast(0L)
                val remainingSec = ceil(remainingMs / 1000.0).toInt().coerceAtLeast(0)
                val progress = if (totalMs > 0L) (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f) else 0f
                _uiState.update {
                    it.copy(offerCountdownSeconds = remainingSec, offerCountdownProgress = progress)
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
        if (isProcessingOffer || _uiState.value.isOfferAccepting || _uiState.value.isOfferRejecting) return
        val currentOffer = _uiState.value.currentFoodOffer
        if (currentOffer == null || currentOffer.orderId != targetOrderId) return

        isProcessingOffer = true
        cancelCountdown()

        viewModelScope.launch {
            _uiState.update { it.copy(isOfferAccepting = true, errorMessage = null) }
            acceptFoodOrderUseCase(targetOrderId).onSuccess {
                _uiState.update {
                    it.copy(
                        currentFoodOffer = null,
                        isOfferAccepting = false,
                        acceptedOrderId = targetOrderId,
                        acceptedOfferRestaurantLat = currentOffer.restaurantLatitude,
                        acceptedOfferRestaurantLng = currentOffer.restaurantLongitude
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

    fun checkActiveDeliveries() {
        viewModelScope.launch {
            if (_uiState.value.acceptedOrderId != null || isProcessingOffer) return@launch
            getDriverActiveFoodOrdersUseCase().onSuccess { orders ->
                val activeOrder = orders.firstOrNull { it.status.isActive }
                _uiState.update { it.copy(activeFoodOrder = activeOrder) }
                if (activeOrder != null && activeOrder.orderId > 0L && _uiState.value.acceptedOrderId != activeOrder.orderId) {
                    _uiState.update {
                        it.copy(
                            acceptedOrderId = activeOrder.orderId,
                            acceptedOfferRestaurantLat = activeOrder.restaurantLatitude,
                            acceptedOfferRestaurantLng = activeOrder.restaurantLongitude
                        )
                    }
                }
            }
        }
    }

    fun onRejectOffer(orderId: Long? = null) {
        val targetOrderId = orderId ?: _uiState.value.currentFoodOffer?.orderId ?: return
        executeReject(targetOrderId, isTimeout = false)
    }

    private fun executeReject(orderId: Long, isTimeout: Boolean) {
        if (isProcessingOffer || _uiState.value.isOfferRejecting || _uiState.value.isOfferAccepting) return
        val currentOffer = _uiState.value.currentFoodOffer
        if (currentOffer == null || currentOffer.orderId != orderId) return

        isProcessingOffer = true
        cancelCountdown()

        viewModelScope.launch {
            _uiState.update { it.copy(isOfferRejecting = true) }
            rejectFoodOrderUseCase(orderId).onSuccess {
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

    fun dismissApprovalWarningDialog() = _uiState.update { it.copy(showApprovalWarningDialog = false) }
    fun clearErrorMessage() = _uiState.update { it.copy(errorMessage = null) }
    fun clearOfferActionMessage() = _uiState.update { it.copy(offerActionMessage = null) }

    fun clearAcceptedOrderId() {
        _uiState.update {
            it.copy(acceptedOrderId = null, acceptedOfferRestaurantLat = null, acceptedOfferRestaurantLng = null)
        }
    }

    fun onAcceptRideOffer(bookingId: Long? = null) {
        rideOfferCoordinator.onAcceptOffer(viewModelScope, bookingId, { _uiState.update(it) }, { _uiState.value })
    }

    fun onRejectRideOffer(bookingId: Long? = null) {
        rideOfferCoordinator.onRejectOffer(viewModelScope, bookingId, { _uiState.update(it) }, { _uiState.value })
    }

    fun clearAcceptedRideBookingId() = _uiState.update { it.copy(acceptedRideBookingId = null) }

    override fun onCleared() {
        super.onCleared()
        offersJob?.cancel()
        countdownJob?.cancel()
        rideOfferCoordinator.cancelCountdown()
    }

    companion object {
        const val ERROR_LOCATION_PERMISSION_REQUIRED = "ERROR_LOCATION_PERMISSION_REQUIRED"
        const val ERROR_GPS_REQUIRED = "ERROR_GPS_REQUIRED"
        const val TIMEOUT_MESSAGE = "TIMEOUT_OFFER_REJECTED"
    }
}
