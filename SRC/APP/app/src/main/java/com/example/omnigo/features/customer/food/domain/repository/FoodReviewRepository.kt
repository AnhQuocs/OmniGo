package com.example.omnigo.features.customer.food.domain.repository

import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import okhttp3.MultipartBody

interface FoodReviewRepository {
    suspend fun uploadReviewImage(file: MultipartBody.Part): Result<String>
    suspend fun submitFoodReview(command: CreateFoodReviewCommand): Result<FoodReview>
    suspend fun getFoodReviewByOrderId(orderId: Long): Result<FoodReview?>
    suspend fun getRestaurantReviews(
        restaurantId: Long,
        page: Int = 0,
        size: Int = 10,
        rating: Int? = null
    ): Result<List<FoodReview>>
}
