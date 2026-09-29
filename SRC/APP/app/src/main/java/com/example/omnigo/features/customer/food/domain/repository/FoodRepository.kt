package com.example.omnigo.features.customer.food.domain.repository

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderRequest
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult

interface FoodRepository {
    suspend fun getRestaurants(search: String? = null): GetRestaurantsResult
    suspend fun getRestaurantDetail(id: Long): GetRestaurantDetailResult
    suspend fun getRestaurantItems(restaurantId: Long, availableOnly: Boolean = false): GetMenuItemsResult
    suspend fun createFoodOrder(request: FoodOrderRequest): CreateFoodOrderResult
}
