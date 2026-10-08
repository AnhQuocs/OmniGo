package com.example.omnigo.features.customer.food.domain.repository



import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand

import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult

import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult



import com.example.omnigo.features.customer.food.domain.model.CancelFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.RetryDriverResult
import com.example.omnigo.features.customer.food.domain.model.SwitchToCashResult

interface FoodRepository {

    suspend fun getRestaurants(search: String? = null): GetRestaurantsResult

    suspend fun getRestaurantDetail(id: Long): GetRestaurantDetailResult

    suspend fun getRestaurantItems(restaurantId: Long, availableOnly: Boolean = false): GetMenuItemsResult

    suspend fun createFoodOrder(command: FoodOrderCreateCommand): CreateFoodOrderResult

    suspend fun getFoodOrderDetail(orderId: Long): GetFoodOrderDetailResult

    suspend fun getMyFoodOrders(): GetMyFoodOrdersResult

    suspend fun cancelFoodOrder(orderId: Long, reasonCode: String? = null, reason: String? = null): CancelFoodOrderResult

    suspend fun switchToCash(orderId: Long): SwitchToCashResult

    suspend fun retryDriver(orderId: Long): RetryDriverResult

    suspend fun getDriverProfile(driverId: Long): GetDriverProfileResult

}
