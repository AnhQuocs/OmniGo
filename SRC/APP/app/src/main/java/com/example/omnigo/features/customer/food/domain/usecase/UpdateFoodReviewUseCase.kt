package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import javax.inject.Inject

class UpdateFoodReviewUseCase @Inject constructor(
    private val foodReviewRepository: FoodReviewRepository
) {
    suspend operator fun invoke(
        reviewId: Long,
        command: CreateFoodReviewCommand
    ): Result<FoodReview> {
        if (command.restaurantRating !in 1..5 || command.driverRating !in 1..5) {
            return Result.failure(IllegalArgumentException("Rating must be between 1 and 5"))
        }
        return foodReviewRepository.updateFoodReview(reviewId, command)
    }
}
