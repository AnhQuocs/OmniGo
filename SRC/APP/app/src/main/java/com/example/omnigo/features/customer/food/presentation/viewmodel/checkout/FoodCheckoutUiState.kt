package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.utils.UiText

data class FoodCheckoutUiState(
    val isLoading: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val restaurant: Restaurant? = null,
    val restaurantId: Long? = null,
    val restaurantName: String = "",
    val items: List<CartItem> = emptyList(),
    val itemsSubtotal: Double = 0.0,
    val deliveryFee: Double = 15000.0,
    val totalAmount: Double = 15000.0,
    val deliveryAddress: String = "",
    val deliveryLatitude: Double = 0.0,
    val deliveryLongitude: Double = 0.0,
    val driverNote: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val errorMessage: UiText? = null,
    val createdOrderId: Long? = null
)
