package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.CartAddResult
import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.utils.UiText

data class RestaurantDetailUiState(
    val isRestaurantLoading: Boolean = false,
    val isMenuLoading: Boolean = false,
    val restaurant: Restaurant? = null,
    val errorMessage: UiText? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val menuItems: Map<String, List<MenuItem>> = emptyMap(),
    val cartItems: Map<Long, CartItem> = emptyMap(),
    val totalCartAmount: Double = 0.0,
    val totalCartQuantity: Int = 0,
    val distanceKm: Double? = null,
    val estimatedDeliveryMinutes: Int? = null,
    val pendingConflict: CartAddResult.RestaurantConflict? = null
)
