package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.util.RestaurantSort
import com.example.omnigo.utils.UiText

data class FoodUiState(
    val isLoading: Boolean = false,
    val restaurants: List<Restaurant> = emptyList(),
    val displayedRestaurants: List<Restaurant> = emptyList(),
    val featuredRestaurants: List<Restaurant> = emptyList(),
    val categories: List<String> = emptyList(),
    val distancesKm: Map<Long, Double> = emptyMap(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedSort: RestaurantSort = RestaurantSort.RECOMMENDED,
    val errorMessage: UiText? = null,
    val cartRestaurantId: Long? = null,
    val totalCartQuantity: Int = 0,
    val totalCartAmount: Double = 0.0,
    val userAddressLabel: String? = null
) {
    val isFiltered: Boolean get() = !selectedCategory.isNullOrBlank() || searchQuery.isNotBlank()
    val showFeaturedSection: Boolean
        get() = !isLoading && errorMessage == null && !isFiltered && featuredRestaurants.isNotEmpty()
}
