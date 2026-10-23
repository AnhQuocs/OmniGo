package com.example.omnigo.features.customer.food.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.core.network.dto.PageResponse
import com.example.omnigo.features.customer.food.data.remote.dto.CreateFoodReviewRequest
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderReviewResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodReviewApi {

    @Multipart
    @POST(ApiEndpoints.FOOD_REVIEW_UPLOAD_IMAGE)
    suspend fun uploadReviewImage(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>

    @POST(ApiEndpoints.FOOD_REVIEWS)
    suspend fun createFoodReview(
        @Body request: CreateFoodReviewRequest
    ): ApiResponse<FoodOrderReviewResponse>

    @GET(ApiEndpoints.FOOD_REVIEW_BY_ORDER)
    suspend fun getFoodReviewByOrderId(
        @Path("orderId") orderId: Long
    ): ApiResponse<FoodOrderReviewResponse>

    @GET(ApiEndpoints.RESTAURANT_REVIEWS)
    suspend fun getRestaurantReviews(
        @Path("restaurantId") restaurantId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("rating") rating: Int? = null
    ): ApiResponse<PageResponse<FoodOrderReviewResponse>>
}
