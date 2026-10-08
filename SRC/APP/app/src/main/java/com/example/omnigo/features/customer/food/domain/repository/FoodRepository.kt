package com.example.omnigo.features.customer.food.domain.repository



import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand

import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult

import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult



interface FoodRepository {

    suspend fun getRestaurants(search: String? = null): GetRestaurantsResult

    suspend fun getRestaurantDetail(id: Long): GetRestaurantDetailResult

    suspend fun getRestaurantItems(restaurantId: Long, availableOnly: Boolean = false): GetMenuItemsResult

    suspend fun createFoodOrder(command: FoodOrderCreateCommand): CreateFoodOrderResult

    suspend fun getFoodOrderDetail(orderId: Long): GetFoodOrderDetailResult

    suspend fun getDriverProfile(driverId: Long): GetDriverProfileResult

}
