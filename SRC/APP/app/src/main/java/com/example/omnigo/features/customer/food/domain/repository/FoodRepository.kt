package com.example.omnigo.features.customer.food.domain.repository

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult

interface FoodRepository {
    suspend fun getRestaurants(search: String? = null): GetRestaurantsResult
}
