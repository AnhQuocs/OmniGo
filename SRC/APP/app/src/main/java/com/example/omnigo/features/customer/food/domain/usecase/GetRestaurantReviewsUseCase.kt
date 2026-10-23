package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import javax.inject.Inject

class GetRestaurantReviewsUseCase @Inject constructor(
    private val foodReviewRepository: FoodReviewRepository
) {
    suspend operator fun invoke(
        restaurantId: Long,
        page: Int = 0,
        size: Int = 10,
        rating: Int? = null
    ): Result<List<FoodReview>> {
        if (restaurantId <= 0) {
            return Result.failure(IllegalArgumentException("Invalid restaurantId"))
        }
        return foodReviewRepository.getRestaurantReviews(restaurantId, page, size, rating)
    }
}
