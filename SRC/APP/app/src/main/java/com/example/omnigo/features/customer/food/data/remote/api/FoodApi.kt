package com.example.omnigo.features.customer.food.data.remote.api



import com.example.omnigo.core.network.ApiEndpoints

import com.example.omnigo.core.network.dto.ApiResponse

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderRequest
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse

import com.example.omnigo.features.customer.food.data.remote.dto.DriverProfileResponse

import com.example.omnigo.features.customer.food.data.remote.dto.MenuItemResponse

import com.example.omnigo.features.customer.food.data.remote.dto.RestaurantResponse

import retrofit2.http.Body

import retrofit2.http.GET

import retrofit2.http.POST

import retrofit2.http.Path

import retrofit2.http.Query



import com.example.omnigo.features.customer.food.data.remote.dto.CancelFoodOrderRequest
import retrofit2.http.PATCH

interface FoodApi {

    @GET(ApiEndpoints.RESTAURANTS)
    suspend fun getRestaurants(
        @Query("search") search: String? = null
    ): ApiResponse<List<RestaurantResponse>>

    @GET(ApiEndpoints.RESTAURANT_DETAIL)
    suspend fun getRestaurantDetail(
        @Path("id") id: Long
    ): ApiResponse<RestaurantResponse>

    @GET(ApiEndpoints.RESTAURANT_ITEMS)
    suspend fun getRestaurantItems(
        @Path("id") restaurantId: Long,
        @Query("availableOnly") availableOnly: Boolean? = null
    ): ApiResponse<List<MenuItemResponse>>

    @POST(ApiEndpoints.FOOD_ORDERS)
    suspend fun createFoodOrder(
        @Body request: FoodOrderRequest
    ): ApiResponse<FoodOrderResponse>

    @GET(ApiEndpoints.FOOD_ORDER_DETAIL)
    suspend fun getFoodOrderDetail(
        @Path("orderId") orderId: Long
    ): ApiResponse<FoodOrderResponse>

    @GET(ApiEndpoints.FOOD_ORDERS_MY)
    suspend fun getMyFoodOrders(): ApiResponse<List<FoodOrderResponse>>

    @POST(ApiEndpoints.FOOD_ORDER_CANCEL)
    suspend fun cancelFoodOrder(
        @Path("orderId") orderId: Long,
        @Body request: CancelFoodOrderRequest? = null
    ): ApiResponse<FoodOrderResponse>

    @PATCH(ApiEndpoints.FOOD_ORDER_SWITCH_TO_CASH)
    suspend fun switchToCash(
        @Path("orderId") orderId: Long
    ): ApiResponse<FoodOrderResponse>

    @POST(ApiEndpoints.FOOD_ORDER_RETRY_DRIVER)
    suspend fun retryDriver(
        @Path("orderId") orderId: Long
    ): ApiResponse<FoodOrderResponse>

    @GET(ApiEndpoints.DRIVER_PROFILE)
    suspend fun getDriverProfile(
        @Path("id") driverId: Long
    ): ApiResponse<DriverProfileResponse>
}
