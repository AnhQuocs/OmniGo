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
