package com.example.omnigo.features.customer.food.data.repository

import android.util.Log
import com.example.omnigo.features.customer.food.data.mapper.toDomain
import com.example.omnigo.features.customer.food.data.mapper.toRequest
import com.example.omnigo.features.customer.food.data.remote.api.FoodReviewApi
import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.DriverFoodReviewsSummary
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import okhttp3.MultipartBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodReviewRepositoryImpl @Inject constructor(
    private val foodReviewApi: FoodReviewApi
) : FoodReviewRepository {

    companion object {
        private const val TAG = "FoodReviewRepo"
    }

    override suspend fun uploadReviewImage(file: MultipartBody.Part): Result<String> {
        return try {
            val response = foodReviewApi.uploadReviewImage(file)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                val url = apiResponse?.data?.get("url")
                    ?: apiResponse?.data?.get("secure_url")
                if (!url.isNullOrBlank()) {
                    Result.success(url)
                } else {
                    Result.failure(Exception(apiResponse?.message ?: "Upload image failed: URL is empty"))
                }
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                val serverMsg = errorBody?.let {
                    try {
                        val json = com.google.gson.JsonParser.parseString(it).asJsonObject
                        json.get("message")?.asString
                    } catch (_: Exception) {
                        null
                    }
                }
                Result.failure(Exception(serverMsg ?: "Upload image failed with HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadReviewImage: failed", e)
            Result.failure(e)
        }
    }

    override suspend fun submitFoodReview(command: CreateFoodReviewCommand): Result<FoodReview> {
        return try {
            val response = foodReviewApi.createFoodReview(command.toRequest())
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "Failed to submit food review"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitFoodReview: failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateFoodReview(
        reviewId: Long,
        command: CreateFoodReviewCommand
    ): Result<FoodReview> {
        return try {
            val response = foodReviewApi.updateFoodReview(reviewId, command.toRequest())
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "Failed to update food review"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateFoodReview: failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getFoodReviewByOrderId(orderId: Long): Result<FoodReview?> {
        return try {
            val response = foodReviewApi.getFoodReviewByOrderId(orderId)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.success(null)
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Result.success(null)
            } else {
                Log.e(TAG, "getFoodReviewByOrderId: http error", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFoodReviewByOrderId: failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getDriverFoodReviews(driverId: Long): Result<DriverFoodReviewsSummary> {
        return try {
            val response = foodReviewApi.getDriverFoodReviews(driverId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                val data = body.data
                val reviews = data.reviews?.map { it.toDomain() } ?: emptyList()
                Result.success(
                    DriverFoodReviewsSummary(
                        driverId = data.driverId ?: driverId,
                        totalReviews = data.totalReviews ?: reviews.size,
                        averageRating = data.averageRating ?: 0.0,
                        reviews = reviews
                    )
                )
            } else {
                Result.failure(Exception(body?.message ?: "Failed to load driver food reviews"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getDriverFoodReviews: failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getRestaurantReviews(
        restaurantId: Long,
        page: Int,
        size: Int,
        rating: Int?
    ): Result<List<FoodReview>> {
        return try {
            val response = foodReviewApi.getRestaurantReviews(
                restaurantId = restaurantId,
                page = page,
                size = size,
                rating = rating
            )
            if (response.success) {
                val reviews = response.data?.reviews?.map { it.toDomain() } ?: emptyList()
                Result.success(reviews)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load restaurant reviews"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRestaurantReviews: failed", e)
            Result.failure(e)
        }
    }
}
