package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.utils.UiText

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)

data class RestaurantDetailUiState(
    val isLoading: Boolean = false,
    val restaurant: Restaurant? = null,
    val errorMessage: UiText? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val cartItems: Map<Long, CartItem> = emptyMap(),
    val totalCartAmount: Double = 0.0,
    val totalCartQuantity: Int = 0,
    val distanceKm: Double? = null,
    val estimatedDeliveryMinutes: Int? = null
)
