package com.example.omnigo.features.driver.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.driver.data.remote.dto.DriverAccountProfileResponse
import com.example.omnigo.features.driver.data.remote.dto.DriverLocationRequest
import com.example.omnigo.features.driver.data.remote.dto.DriverStatusUpdateRequest
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse
import com.example.omnigo.features.driver.food.data.remote.dto.UpdateDriverOrderStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DriverApi {

    @PUT(ApiEndpoints.DRIVER_STATUS)
    suspend fun updateDriverStatus(
        @Path("driverId") driverId: Long,
        @Body request: DriverStatusUpdateRequest
    ): ApiResponse<Any>

    @PUT(ApiEndpoints.DRIVER_STATUS_ME)
    suspend fun updateMyDriverStatus(
        @Body request: DriverStatusUpdateRequest
    ): ApiResponse<Any>

    @GET(ApiEndpoints.DRIVER_PROFILE_BY_ID)
    suspend fun getDriverProfileById(
        @Path("driverId") driverId: Long
    ): ApiResponse<DriverAccountProfileResponse>

    @GET(ApiEndpoints.DRIVER_PROFILE_ME)
    suspend fun getMyDriverProfile(): ApiResponse<DriverAccountProfileResponse>

    @GET(ApiEndpoints.DRIVER_PROFILE)
    suspend fun getDriverProfileInternal(
        @Path("id") id: Long
    ): ApiResponse<DriverAccountProfileResponse>

    @POST(ApiEndpoints.DRIVER_LOCATION_ME)
    suspend fun sendDriverLocation(
        @Body request: DriverLocationRequest
    ): ApiResponse<Any>

    @POST(ApiEndpoints.BOOKING_FOOD_ORDER_ACCEPT)
    suspend fun acceptFoodOrder(
        @Path("orderId") orderId: Long
    ): ApiResponse<String>

    @POST(ApiEndpoints.BOOKING_FOOD_ORDER_REJECT)
    suspend fun rejectFoodOrder(
        @Path("orderId") orderId: Long
    ): ApiResponse<String>

    @GET(ApiEndpoints.FOOD_ORDERS_DRIVER)
    suspend fun getDriverFoodOrders(): ApiResponse<List<FoodOrderResponse>>

    @GET(ApiEndpoints.FOOD_ORDER_DETAIL)
    suspend fun getFoodOrderDetail(
        @Path("orderId") orderId: Long
    ): ApiResponse<FoodOrderResponse>

    @PATCH(ApiEndpoints.FOOD_ORDER_DRIVER_STATUS)
    suspend fun updateDriverOrderStatus(
        @Path("orderId") orderId: Long,
        @Body request: UpdateDriverOrderStatusRequest
    ): ApiResponse<FoodOrderResponse>
}
