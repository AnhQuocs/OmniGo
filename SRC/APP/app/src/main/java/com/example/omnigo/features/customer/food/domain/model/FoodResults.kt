package com.example.omnigo.features.customer.food.domain.model

import com.example.omnigo.features.customer.food.domain.error.FoodError

sealed interface GetRestaurantsResult {
    data class Success(
        val restaurants: List<Restaurant>
    ) : GetRestaurantsResult

    data class Error(
        val error: FoodError
    ) : GetRestaurantsResult
}

sealed interface GetRestaurantDetailResult {
    data class Success(
        val restaurant: Restaurant
    ) : GetRestaurantDetailResult

    data class Error(
        val error: FoodError
    ) : GetRestaurantDetailResult
}

sealed interface GetMenuItemsResult {
    data class Success(
        val items: List<MenuItem>
    ) : GetMenuItemsResult

    data class Error(
        val error: FoodError
    ) : GetMenuItemsResult
}

sealed interface CreateFoodOrderResult {
    data class Success(
        val order: FoodOrder
    ) : CreateFoodOrderResult

    data class Error(
        val error: FoodError,
        val message: String? = null
    ) : CreateFoodOrderResult
}
