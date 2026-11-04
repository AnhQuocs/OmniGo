package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.data.local.FoodOrderVoucherStore
import com.example.omnigo.features.customer.food.domain.model.*
import com.example.omnigo.features.customer.food.domain.repository.ActiveFoodOrderTrackingRepository
import com.example.omnigo.features.customer.food.domain.usecase.*
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideVoucherReviewCoordinator
import com.example.omnigo.features.driver.earnings.domain.usecase.CreateOrderPaymentUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetFoodOrderPaymentStatusUseCase
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodOrderDetailViewModel @Inject constructor(
    private val getFoodOrderDetailUseCase: GetFoodOrderDetailUseCase,
    private val getDriverProfileUseCase: GetDriverProfileUseCase,
    private val cancelFoodOrderUseCase: CancelFoodOrderUseCase,
    private val switchToCashUseCase: SwitchToCashUseCase,
    private val retryDriverUseCase: RetryDriverUseCase,
    private val activeTrackingRepository: ActiveFoodOrderTrackingRepository,
    savedStateHandle: SavedStateHandle,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val getRestaurantItemsUseCase: GetRestaurantItemsUseCase,
    private val voucherCoordinator: RideVoucherReviewCoordinator? = null,
    private val createOrderPaymentUseCase: CreateOrderPaymentUseCase? = null,
    private val getFoodOrderPaymentStatusUseCase: GetFoodOrderPaymentStatusUseCase? = null,
    private val voucherStore: FoodOrderVoucherStore? = null
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L
    private val _uiState = MutableStateFlow(FoodOrderDetailUiState())
    val uiState: StateFlow<FoodOrderDetailUiState> = _uiState.asStateFlow()
    private var driverProfileJob: Job? = null
    private var trackingStateJob: Job? = null
    private var paymentPollingJob: Job? = null

    init {
        loadOrderDetail(orderId)
        if (orderId > 0) observeRealtimeTracking(orderId)
    }

    private fun observeRealtimeTracking(id: Long) {
        if (id <= 0) return
        trackingStateJob?.cancel()
        viewModelScope.launch { activeTrackingRepository.activate(id) }
        trackingStateJob = viewModelScope.launch {
            activeTrackingRepository.activeTrackingState.collect { trackingState ->
                if (trackingState != null && trackingState.orderId == id) {
                    _uiState.update { current ->
                        val updatedOrder = current.order?.let { order ->
                            var modified = order
                            if (!trackingState.status.isNullOrBlank() && trackingState.status != modified.status) {
                                modified = modified.copy(status = trackingState.status)
                            }
                            val profile = trackingState.driverProfile
                            if (profile != null && modified.driverId != profile.driverId) {
                                modified = modified.copy(
                                    driverId = profile.driverId,
                                    driverName = profile.driverName ?: modified.driverName,
                                    driverPhone = profile.driverPhone ?: modified.driverPhone,
                                    driverVehiclePlate = profile.vehiclePlate ?: modified.driverVehiclePlate
                                )
                            }
                            modified
                        }
                        current.copy(
                            order = updatedOrder,
                            driverProfile = trackingState.driverProfile ?: current.driverProfile,
                            driverLocation = trackingState.driverLocation ?: current.driverLocation
                        )
                    }
                }
            }
        }
    }

    fun loadOrderDetail(id: Long = orderId, isRefresh: Boolean = false) {
        if (id <= 0) {
            _uiState.update {
                it.copy(
                    isLoading = false, isRefreshing = false,
                    errorMessage = UiText.StringResource(R.string.order_detail_invalid_order_id), canRetry = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isLoading = it.order == null, isRefreshing = true, errorMessage = null)
                else it.copy(isLoading = true, isRefreshing = false, errorMessage = null)
            }

            when (val result = getFoodOrderDetailUseCase(id)) {
                is GetFoodOrderDetailResult.Success -> {
                    _uiState.update { current ->
                        val wasPaid = current.order?.isPaid == true
                        val isPaidNow = result.order.isPaid == true || wasPaid
                        val paidOrder = if (isPaidNow && result.order.isPaid != true) {
                            result.order.copy(isPaid = true)
                        } else {
                            result.order
                        }
                        val resolvedOrder = voucherStore?.get(paidOrder.id)?.takeIf { paidOrder.originalAmount == null }?.let { (code, discount) ->
                            paidOrder.copy(voucherCode = code, voucherDiscount = discount)
                        } ?: paidOrder
                        current.copy(
                            isLoading = false, isRefreshing = false, order = resolvedOrder,
                            driverProfile = if (current.order?.driverId == resolvedOrder.driverId) current.driverProfile else null,
                            errorMessage = null, canRetry = true
                        )
                    }
                    loadDriverProfile(result.order.driverId)
                    enrichOrderWithRestaurantData(result.order)
                    if (!isRefresh && result.order.paymentMethod != PaymentMethod.CASH && result.order.isPaid != true && _uiState.value.order?.isPaid != true) {
                        pollPaymentStatus(result.order.id, maxAttempts = 3, showPendingNotice = false)
                    }
                }
                is GetFoodOrderDetailResult.Error -> {
                    val errorUiText = result.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString) ?: result.error.asUiText()
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = errorUiText, canRetry = true) }
                }
            }
        }
    }

    private fun loadDriverProfile(driverId: Long?) {
        driverProfileJob?.cancel()
        if (driverId == null || driverId <= 0) {
            _uiState.update { it.copy(driverProfile = null, isDriverProfileLoading = false, driverProfileError = null) }
            return
        }

        _uiState.update { it.copy(isDriverProfileLoading = true, driverProfileError = null) }
        driverProfileJob = viewModelScope.launch {
            when (val result = getDriverProfileUseCase(driverId)) {
                is GetDriverProfileResult.Success -> {
                    _uiState.update {
                        if (it.order?.driverId == driverId) it.copy(driverProfile = result.profile, isDriverProfileLoading = false, driverProfileError = null) else it
                    }
                }
                is GetDriverProfileResult.Error -> {
                    _uiState.update {
                        if (it.order?.driverId == driverId) {
                            val err = result.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString) ?: result.error.asUiText()
                            it.copy(isDriverProfileLoading = false, driverProfileError = err)
                        } else it
                    }
                }
            }
        }
    }

    fun onOpenCancelDialog() = _uiState.update { it.copy(isCancelDialogOpen = true) }
    fun onDismissCancelDialog() = _uiState.update { it.copy(isCancelDialogOpen = false) }

    fun onCancelOrder(reasonCode: String?, reason: String?) {
        if (orderId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, isCancelDialogOpen = false) }
            when (val result = cancelFoodOrderUseCase(orderId, reasonCode, reason)) {
                is CancelFoodOrderResult.Success -> {
                    _uiState.update { it.copy(isActionLoading = false, order = result.order, actionMessage = UiText.StringResource(R.string.order_cancel_success)) }
                }
                is CancelFoodOrderResult.Error -> {
                    val err = result.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString) ?: result.error.asUiText()
                    _uiState.update { it.copy(isActionLoading = false, actionMessage = err) }
                }
            }
        }
    }

    fun onSwitchToCash() {
        if (orderId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            when (val result = switchToCashUseCase(orderId)) {
                is SwitchToCashResult.Success -> {
                    _uiState.update { it.copy(isActionLoading = false, order = result.order, actionMessage = UiText.StringResource(R.string.order_switch_cash_success)) }
                }
                is SwitchToCashResult.Error -> {
                    val err = result.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString) ?: result.error.asUiText()
                    _uiState.update { it.copy(isActionLoading = false, actionMessage = err) }
                }
            }
        }
    }

    fun onPayOnlineNow() {
        val order = _uiState.value.order ?: return
        val amount = order.totalPrice ?: (order.itemsPrice + (order.deliveryFee ?: 0.0))
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            createOrderPaymentUseCase?.invoke(
                bookingId = order.id,
                amount = amount,
                method = order.paymentMethod?.name ?: "VNPAY",
                type = "FOOD_PAYMENT"
            )?.onSuccess { payUrl ->
                when {
                    payUrl.equals("WALLET_SUCCESS", ignoreCase = true) -> {
                        _uiState.update {
                            it.copy(isActionLoading = false, openPaymentUrl = null, actionMessage = UiText.StringResource(R.string.payment_sentinel_wallet_success))
                        }
                        pollPaymentStatus(order.id, maxAttempts = 3)
                    }
                    payUrl.equals("ALREADY_PAID", ignoreCase = true) -> {
                        _uiState.update {
                            it.copy(isActionLoading = false, openPaymentUrl = null, actionMessage = UiText.StringResource(R.string.payment_sentinel_already_paid))
                        }
                        loadOrderDetail(order.id, isRefresh = true)
                    }
                    !payUrl.isNullOrBlank() -> _uiState.update { it.copy(isActionLoading = false, openPaymentUrl = payUrl) }
                    else -> _uiState.update { it.copy(isActionLoading = false) }
                }
            }?.onFailure { err ->
                _uiState.update { it.copy(isActionLoading = false, actionMessage = UiText.DynamicString(err.message ?: "Payment failed")) }
            } ?: _uiState.update { it.copy(isActionLoading = false) }
        }
    }

    fun onCheckPaymentStatus(autoPolling: Boolean = false) {
        val id = _uiState.value.order?.id ?: orderId
        if (id <= 0) return
        pollPaymentStatus(id, maxAttempts = if (autoPolling) 3 else 1, showPendingNotice = !autoPolling)
    }

    private fun pollPaymentStatus(id: Long, maxAttempts: Int, showPendingNotice: Boolean = true) {
        paymentPollingJob?.cancel()
        paymentPollingJob = viewModelScope.launch {
            if (showPendingNotice) _uiState.update { it.copy(isActionLoading = true) }
            var attempts = 0
            var isSuccess = false

            while (attempts < maxAttempts && !isSuccess) {
                if (attempts > 0) delay(2000)
                attempts++
                getFoodOrderPaymentStatusUseCase?.invoke(id)?.onSuccess { statusDto ->
                    if (statusDto.paid) {
                        isSuccess = true
                        _uiState.update { current ->
                            current.copy(
                                isActionLoading = false,
                                actionMessage = UiText.StringResource(R.string.order_payment_status_paid),
                                order = current.order?.copy(isPaid = true)
                            )
                        }
                        delay(1200)
                        loadOrderDetail(id, isRefresh = true)
                    }
                }
            }

            if (!isSuccess && showPendingNotice) {
                val finalMsg = if (maxAttempts > 1) {
                    UiText.StringResource(R.string.payment_status_pending_hint)
                } else {
                    UiText.StringResource(R.string.order_payment_status_unpaid, "PENDING")
                }
                _uiState.update { it.copy(isActionLoading = false, actionMessage = finalMsg) }
            } else if (!isSuccess) {
                _uiState.update { it.copy(isActionLoading = false) }
            }
        }
    }

    fun clearOpenPaymentUrl() = _uiState.update { it.copy(openPaymentUrl = null) }

    fun onRetryDriver() {
        if (orderId <= 0) return
        if (_uiState.value.retryDriverCount >= 3) {
            _uiState.update { it.copy(actionMessage = UiText.StringResource(R.string.order_detail_retry_driver_max_reached)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            when (val result = retryDriverUseCase(orderId)) {
                is RetryDriverResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false, order = result.order, retryDriverCount = it.retryDriverCount + 1,
                            actionMessage = UiText.StringResource(R.string.order_retry_driver_success)
                        )
                    }
                }
                is RetryDriverResult.Error -> {
                    val err = result.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString) ?: result.error.asUiText()
                    _uiState.update { it.copy(isActionLoading = false, actionMessage = err) }
                }
            }
        }
    }

    fun onClearActionMessage() = _uiState.update { it.copy(actionMessage = null) }
    fun onRetry() = loadOrderDetail(orderId)
    fun onClearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun enrichOrderWithRestaurantData(order: FoodOrder) {
        val restaurantId = order.restaurantId
        if (restaurantId <= 0) return

        viewModelScope.launch {
            var restaurantImageUrl: String? = null
            var restaurantAddress: String? = null
            var catalogItems: List<MenuItem> = emptyList()

            when (val res = getRestaurantDetailUseCase(restaurantId)) {
                is GetRestaurantDetailResult.Success -> {
                    restaurantImageUrl = res.restaurant.imageUrl.takeIf { it.isNotBlank() }
                    restaurantAddress = res.restaurant.address.takeIf { it.isNotBlank() }
                    catalogItems = res.restaurant.menuItems
                }
                else -> Unit
            }

            if (catalogItems.isEmpty() || catalogItems.none { it.imageUrl.isNotBlank() }) {
                when (val menuRes = getRestaurantItemsUseCase(restaurantId, availableOnly = false)) {
                    is GetMenuItemsResult.Success -> catalogItems = menuRes.items
                    else -> Unit
                }
            }

            _uiState.update { current ->
                val curOrder = current.order ?: return@update current
                if (curOrder.id != order.id) return@update current

                val enrichedItems = curOrder.items.map { item ->
                    val match = catalogItems.firstOrNull {
                        it.id == item.menuItemId || it.name.trim().equals(item.itemName.trim(), ignoreCase = true)
                    }
                    val realImage = item.imageUrl?.takeIf { it.isNotBlank() } ?: match?.imageUrl
                    if (!realImage.isNullOrBlank() && realImage != item.imageUrl) item.copy(imageUrl = realImage) else item
                }

                current.copy(
                    order = curOrder.copy(
                        restaurantImageUrl = curOrder.restaurantImageUrl?.takeIf { it.isNotBlank() } ?: restaurantImageUrl,
                        restaurantAddress = curOrder.restaurantAddress?.takeIf { it.isNotBlank() } ?: restaurantAddress,
                        items = enrichedItems
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingStateJob?.cancel()
        driverProfileJob?.cancel()
        paymentPollingJob?.cancel()
        viewModelScope.launch { activeTrackingRepository.clear() }
    }
}
