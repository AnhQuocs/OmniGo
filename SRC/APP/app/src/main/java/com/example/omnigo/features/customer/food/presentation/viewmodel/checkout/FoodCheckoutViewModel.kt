package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.CreateFoodOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class FoodCheckoutViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val userLocationManager: UserLocationManager,
    private val createFoodOrderUseCase: CreateFoodOrderUseCase,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodCheckoutUiState())
    val uiState: StateFlow<FoodCheckoutUiState> = _uiState.asStateFlow()

    init {
        observeCartState()
        observeLocationState()
    }

    private fun observeCartState() {
        viewModelScope.launch {
            cartManager.cartState.collect { cart ->
                val itemsList = cart.items.values.toList()
                val subtotal = cart.totalAmount
                val restaurantId = cart.restaurantId
                val restaurantName = cart.restaurantName

                _uiState.update { current ->
                    val fee = calculateDeliveryFee(current.restaurant, current.deliveryLatitude, current.deliveryLongitude)
                    current.copy(
                        restaurantId = restaurantId,
                        restaurantName = restaurantName,
                        items = itemsList,
                        itemsSubtotal = subtotal,
                        deliveryFee = fee,
                        totalAmount = subtotal + fee
                    )
                }

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
                        val fee = calculateDeliveryFee(current.restaurant, location.latitude, location.longitude)
                        current.copy(
                            deliveryAddress = fullAddress,
                            deliveryLatitude = location.latitude,
                            deliveryLongitude = location.longitude,
                            deliveryFee = fee,
                            totalAmount = current.itemsSubtotal + fee
                        )
                    }
                }
            }
        }
    }

    private fun loadRestaurantDetails(restaurantId: Long) {
        viewModelScope.launch {
            when (val result = getRestaurantDetailUseCase(restaurantId)) {
                is GetRestaurantDetailResult.Success -> {
                    _uiState.update { current ->
                        val fee = calculateDeliveryFee(result.restaurant, current.deliveryLatitude, current.deliveryLongitude)
                        current.copy(
                            restaurant = result.restaurant,
                            restaurantName = result.restaurant.name,
                            deliveryFee = fee,
                            totalAmount = current.itemsSubtotal + fee
                        )
                    }
                }
                is GetRestaurantDetailResult.Error -> {
                    // Non-fatal, use basic name from cart
                }
            }
        }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun onDriverNoteChanged(note: String) {
        _uiState.update { it.copy(driverNote = note) }
    }

    fun onDeliveryAddressChanged(address: String, latitude: Double, longitude: Double) {
        _uiState.update { current ->
            val fee = calculateDeliveryFee(current.restaurant, latitude, longitude)
            current.copy(
                deliveryAddress = address,
                deliveryLatitude = latitude,
                deliveryLongitude = longitude,
                deliveryFee = fee,
                totalAmount = current.itemsSubtotal + fee
            )
        }
    }

    fun onPlaceOrder() {
        val state = _uiState.value
        if (state.isPlacingOrder) return
        if (state.items.isEmpty() || state.restaurantId == null) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.checkout_error_empty_cart)) }
            return
        }
        if (state.deliveryAddress.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.checkout_error_empty_address)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null) }

            val result = createFoodOrderUseCase(
                restaurantId = state.restaurantId,
                dropOffAddress = state.deliveryAddress,
                dropOffLatitude = state.deliveryLatitude,
                dropOffLongitude = state.deliveryLongitude,
                items = state.items,
                note = state.driverNote.ifBlank { null },
                paymentMethod = state.selectedPaymentMethod
            )

            when (result) {
                is CreateFoodOrderResult.Success -> {
                    cartManager.clearCart()
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            createdOrderId = result.order.id
                        )
                    }
                }
                is CreateFoodOrderResult.Error -> {
                    val errorUiText = if (!result.message.isNullOrBlank()) {
                        UiText.DynamicString(result.message)
                    } else {
                        result.error.asUiText()
                    }
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            errorMessage = errorUiText
                        )
                    }
                }
            }
        }
    }

    fun onClearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun calculateDeliveryFee(restaurant: Restaurant?, dropOffLat: Double, dropOffLng: Double): Double {
        val baseFee = 15000.0
        if (restaurant == null || (restaurant.latitude == 0.0 && restaurant.longitude == 0.0) || (dropOffLat == 0.0 && dropOffLng == 0.0)) {
            return baseFee
        }

        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(dropOffLat - restaurant.latitude)
        val dLon = Math.toRadians(dropOffLng - restaurant.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(restaurant.latitude)) * cos(Math.toRadians(dropOffLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = r * c

        return if (distanceKm <= 2.0) {
            baseFee
        } else {
            val extraKm = distanceKm - 2.0
            baseFee + (extraKm * 5000.0).toInt()
        }
    }
}
