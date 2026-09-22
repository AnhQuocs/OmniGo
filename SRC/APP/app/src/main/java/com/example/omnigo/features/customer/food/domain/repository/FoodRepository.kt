package com.example.omnigo.features.customer.food.domain.repository

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult

interface FoodRepository {
    suspend fun getRestaurants(search: String? = null): GetRestaurantsResult
    suspend fun getRestaurantDetail(id: Long): GetRestaurantDetailResult
}
