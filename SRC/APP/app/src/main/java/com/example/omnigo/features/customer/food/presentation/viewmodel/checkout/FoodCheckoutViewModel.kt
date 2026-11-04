package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.features.customer.food.data.local.FoodOrderVoucherStore
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.model.RestaurantAvailability
import com.example.omnigo.features.customer.food.domain.usecase.CalculateFoodDeliveryFeeUseCase
import com.example.omnigo.features.customer.food.domain.usecase.CreateFoodOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.util.DeliveryFeeCalculator
import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideVoucherReviewCoordinator
import com.example.omnigo.features.driver.earnings.domain.usecase.CreateOrderPaymentUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.DepositWalletUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverWalletUseCase
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodCheckoutViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val userLocationManager: UserLocationManager,
    private val createFoodOrderUseCase: CreateFoodOrderUseCase,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val voucherCoordinator: RideVoucherReviewCoordinator? = null,
    private val createOrderPaymentUseCase: CreateOrderPaymentUseCase? = null,
    private val getWalletUseCase: GetDriverWalletUseCase? = null,
    private val depositWalletUseCase: DepositWalletUseCase? = null,
    private val calculateFoodDeliveryFeeUseCase: CalculateFoodDeliveryFeeUseCase? = null,
    private val voucherStore: FoodOrderVoucherStore? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodCheckoutUiState())
    val uiState: StateFlow<FoodCheckoutUiState> = _uiState.asStateFlow()
    private var pendingOrderId: Long? = null

    init {
        observeCartState()
        observeLocationState()
        loadWalletBalance()
    }

    private fun observeCartState() {
        viewModelScope.launch {
            cartManager.cartState.collect { cart ->
                val itemsList = cart.items.values.toList()
                val subtotal = cart.totalAmount
                val restaurantId = cart.restaurantId
                val restaurantName = cart.restaurantName

                _uiState.update { current ->
                    current.copy(
                        restaurantId = restaurantId,
                        restaurantName = restaurantName,
                        items = itemsList,
                        itemsSubtotal = subtotal
                    )
                }
                updateDeliveryFee(_uiState.value.restaurant, _uiState.value.deliveryLatitude, _uiState.value.deliveryLongitude)

                if (restaurantId != null && _uiState.value.restaurant?.id != restaurantId) {
                    loadRestaurantDetails(restaurantId)
                }
            }
        }
    }

    private fun observeLocationState() {
        viewModelScope.launch {
            userLocationManager.currentLocation.collect { location ->
                if (location != null && _uiState.value.deliveryAddress.isBlank()) {
                    val fullAddress = location.fullAddress.ifBlank { location.addressName }
                    _uiState.update { current ->
                        current.copy(
                            deliveryAddress = fullAddress,
                            deliveryLatitude = location.latitude,
                            deliveryLongitude = location.longitude
                        )
                    }
                    updateDeliveryFee(_uiState.value.restaurant, location.latitude, location.longitude)
                }
            }
        }
    }

    private fun loadRestaurantDetails(restaurantId: Long) {
        viewModelScope.launch {
            when (val result = getRestaurantDetailUseCase(restaurantId)) {
                is GetRestaurantDetailResult.Success -> {
                    _uiState.update { current ->
                        current.copy(restaurant = result.restaurant, restaurantName = result.restaurant.name)
                    }
                    updateDeliveryFee(result.restaurant, _uiState.value.deliveryLatitude, _uiState.value.deliveryLongitude)
                }
                is GetRestaurantDetailResult.Error -> Unit
            }
        }
    }

    private fun updateDeliveryFee(restaurant: Restaurant?, lat: Double, lng: Double) {
        val subtotal = _uiState.value.itemsSubtotal
        val localFee = DeliveryFeeCalculator.calculate(restaurant, lat, lng)
        _uiState.update { current ->
            val finalFee = if (localFee > 0.0 || (lat != 0.0 && lng != 0.0)) localFee else current.deliveryFee
            current.copy(deliveryFee = finalFee, totalAmount = subtotal + finalFee)
        }

        if (restaurant != null && restaurant.hasCoordinates && lat != 0.0 && lng != 0.0) {
            viewModelScope.launch {
                val remoteFee = calculateFoodDeliveryFeeUseCase?.invoke(restaurant, lat, lng)
                if (remoteFee != null && remoteFee > 0.0) {
                    _uiState.update { current ->
                        current.copy(deliveryFee = remoteFee, totalAmount = current.itemsSubtotal + remoteFee)
                    }
                }
            }
        }
    }

    fun loadWalletBalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWalletLoading = true) }
            val walletRes = getWalletUseCase?.invoke()
            _uiState.update {
                it.copy(
                    isWalletLoading = false,
                    walletBalance = walletRes?.getOrNull()?.balance ?: it.walletBalance ?: 0.0
                )
            }
        }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
        if (method == PaymentMethod.WALLET) loadWalletBalance()
    }

    fun openQuickTopUp(show: Boolean) = _uiState.update { it.copy(showQuickTopUp = show) }

    fun onQuickTopUpConfirm(amount: Double) {
        if (amount < 10000.0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isTopUpLoading = true) }
            depositWalletUseCase?.invoke(amount = amount, method = "VNPAY")
                ?.onSuccess { payUrl ->
                    _uiState.update { it.copy(isTopUpLoading = false, showQuickTopUp = false, paymentWebViewUrl = payUrl) }
                }
                ?.onFailure { err ->
                    _uiState.update { it.copy(isTopUpLoading = false, errorMessage = UiText.DynamicString(err.message ?: "Deposit failed")) }
                } ?: _uiState.update { it.copy(isTopUpLoading = false) }
        }
    }

    fun onPaymentWebViewDismiss() {
        _uiState.update { it.copy(paymentWebViewUrl = null, createdOrderId = pendingOrderId ?: it.createdOrderId) }
        pendingOrderId = null
        loadWalletBalance()
    }

    fun onPaymentWebViewSuccess() {
        _uiState.update { it.copy(paymentWebViewUrl = null, createdOrderId = pendingOrderId ?: it.createdOrderId) }
        pendingOrderId = null
        loadWalletBalance()
    }

    fun onPaymentWebViewFailed(message: String) {
        _uiState.update {
            it.copy(
                paymentWebViewUrl = null,
                errorMessage = UiText.StringResource(R.string.checkout_payment_failed_toast),
                createdOrderId = pendingOrderId ?: it.createdOrderId
            )
        }
        pendingOrderId = null
        loadWalletBalance()
    }

    fun onDriverNoteChanged(note: String) = _uiState.update { it.copy(driverNote = note) }

    fun onDeliveryAddressChanged(address: String, latitude: Double, longitude: Double) {
        _uiState.update { it.copy(deliveryAddress = address, deliveryLatitude = latitude, deliveryLongitude = longitude) }
        updateDeliveryFee(_uiState.value.restaurant, latitude, longitude)
    }

    fun openVoucherSheet() {
        val amount = _uiState.value.totalAmount
        _uiState.update { it.copy(showVoucherSheet = true, isApplyingVoucher = true) }
        viewModelScope.launch {
            val list = voucherCoordinator?.loadApplicableVouchers(amount, "FOOD_DELIVERY") ?: emptyList()
            _uiState.update { it.copy(applicableVouchers = list, isApplyingVoucher = false) }
        }
    }

    fun dismissVoucherSheet() = _uiState.update { it.copy(showVoucherSheet = false) }

    fun applyVoucherCode(code: String) {
        val amount = _uiState.value.totalAmount
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingVoucher = true) }
            voucherCoordinator?.applyVoucher(code = code, orderAmount = amount, serviceType = "FOOD_DELIVERY")
                ?.onSuccess { res ->
                    _uiState.update { it.copy(appliedVoucher = res, isApplyingVoucher = false, showVoucherSheet = false) }
                }
                ?.onFailure { err ->
                    _uiState.update { it.copy(isApplyingVoucher = false, errorMessage = UiText.DynamicString(err.message ?: "Invalid voucher")) }
                } ?: _uiState.update { it.copy(isApplyingVoucher = false) }
        }
    }

    fun removeVoucher() = _uiState.update { it.copy(appliedVoucher = null, showVoucherSheet = false) }

    fun onPlaceOrder() {
        if (_uiState.value.isPlacingOrder) return
        _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null) }

        val state = _uiState.value
        if (state.items.isEmpty() || state.restaurantId == null) {
            _uiState.update { it.copy(isPlacingOrder = false, errorMessage = UiText.StringResource(R.string.checkout_error_empty_cart)) }
            return
        }
        if (state.deliveryAddress.isBlank()) {
            _uiState.update { it.copy(isPlacingOrder = false, errorMessage = UiText.StringResource(R.string.checkout_error_empty_address)) }
            return
        }

        val restaurant = state.restaurant
        if (restaurant != null) {
            if (restaurant.availability == RestaurantAvailability.LOCKED) {
                _uiState.update { it.copy(isPlacingOrder = false, errorMessage = UiText.StringResource(R.string.checkout_error_restaurant_locked)) }
                return
            }
            if (restaurant.availability == RestaurantAvailability.CLOSED) {
                _uiState.update { it.copy(isPlacingOrder = false, errorMessage = UiText.StringResource(R.string.checkout_error_restaurant_closed)) }
                return
            }
        }

        // Pre-check for OmniPay Wallet
        if (state.selectedPaymentMethod == PaymentMethod.WALLET) {
            val balance = state.walletBalance ?: 0.0
            if (balance < state.finalTotalAmount) {
                _uiState.update {
                    it.copy(
                        isPlacingOrder = false,
                        errorMessage = UiText.StringResource(R.string.checkout_error_insufficient_wallet),
                        showQuickTopUp = true
                    )
                }
                return
            }
            // Require explicit confirmation for Wallet payment
            _uiState.update { it.copy(isPlacingOrder = false, showWalletPaymentConfirm = true) }
            return
        }

        executePlaceOrder()
    }

    fun dismissWalletPaymentConfirm() {
        _uiState.update { it.copy(showWalletPaymentConfirm = false) }
    }

    fun onConfirmWalletPayment() {
        _uiState.update { it.copy(showWalletPaymentConfirm = false, isPlacingOrder = true) }
        executePlaceOrder()
    }

    private fun executePlaceOrder() {
        viewModelScope.launch {
            val state = _uiState.value
            val restaurantId = state.restaurantId ?: return@launch
            val result = createFoodOrderUseCase(
                restaurantId = restaurantId,
                dropOffAddress = state.deliveryAddress,
                dropOffLatitude = state.deliveryLatitude,
                dropOffLongitude = state.deliveryLongitude,
                items = state.items,
                note = state.driverNote.ifBlank { null },
                paymentMethod = state.selectedPaymentMethod,
                voucherCode = state.appliedVoucher?.voucherCode
            )

            when (result) {
                is CreateFoodOrderResult.Success -> {
                    var webViewUrl: String? = null
                    var paymentError: UiText? = null
                    if (state.selectedPaymentMethod != PaymentMethod.CASH) {
                        val orderAmount = result.order.totalPrice ?: (result.order.itemsPrice + (result.order.deliveryFee ?: 0.0)).takeIf { it > 0.0 } ?: state.finalTotalAmount
                        val paymentResult = createOrderPaymentUseCase?.invoke(
                            bookingId = result.order.id,
                            amount = orderAmount,
                            method = state.selectedPaymentMethod.name,
                            type = "FOOD_PAYMENT"
                        )
                        val paymentRes = paymentResult?.getOrNull()

                        if (paymentRes?.startsWith("http") == true) {
                            webViewUrl = paymentRes
                        } else if (paymentResult == null || paymentResult.isFailure) {
                            // Order stays AWAITING_PAYMENT, restaurant cannot see it until paid.
                            paymentError = paymentResult?.exceptionOrNull()?.message
                                ?.takeIf { it.isNotBlank() }
                                ?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.checkout_payment_failed_toast)
                        }
                    }

                    cartManager.clearCart()
                    if (!webViewUrl.isNullOrBlank()) {
                        pendingOrderId = result.order.id
                        _uiState.update { it.copy(isPlacingOrder = false, paymentWebViewUrl = webViewUrl) }
                    } else {
                        _uiState.update {
                            it.copy(isPlacingOrder = false, createdOrderId = result.order.id, errorMessage = paymentError)
                        }
                    }
                }
                is CreateFoodOrderResult.Error -> {
                    val errorUiText = if (!result.message.isNullOrBlank()) {
                        UiText.DynamicString(result.message)
                    } else {
                        result.error.asUiText()
                    }
                    _uiState.update { it.copy(isPlacingOrder = false, errorMessage = errorUiText) }
                }
            }
        }
    }
    fun onClearError() = _uiState.update { it.copy(errorMessage = null) }
    fun onOrderCreatedHandled() = _uiState.update { it.copy(createdOrderId = null) }
}
