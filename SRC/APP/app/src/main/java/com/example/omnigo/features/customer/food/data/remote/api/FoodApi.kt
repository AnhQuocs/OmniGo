package com.example.omnigo.features.customer.food.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.food.data.remote.dto.RestaurantResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApi {

    @GET(ApiEndpoints.RESTAURANTS)
    suspend fun getRestaurants(
        @Query("search") search: String? = null
    ): ApiResponse<List<RestaurantResponse>>

    @GET(ApiEndpoints.RESTAURANT_DETAIL)
    suspend fun getRestaurantDetail(
        @Path("id") id: Long
    ): ApiResponse<RestaurantResponse>
}
