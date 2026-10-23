package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import javax.inject.Inject

class SubmitFoodReviewUseCase @Inject constructor(
    private val foodReviewRepository: FoodReviewRepository
) {
    suspend operator fun invoke(command: CreateFoodReviewCommand): Result<FoodReview> {
        if (command.orderId <= 0) {
            return Result.failure(IllegalArgumentException("Invalid orderId"))
        }
        if (command.restaurantRating !in 1..5) {
            return Result.failure(IllegalArgumentException("Restaurant rating must be between 1 and 5"))
        }
        if (command.driverRating !in 1..5) {
            return Result.failure(IllegalArgumentException("Driver rating must be between 1 and 5"))
        }
        if (command.reviewPhotos.size > 5) {
            return Result.failure(IllegalArgumentException("Maximum 5 review photos allowed"))
        }
        return foodReviewRepository.submitFoodReview(command)
    }
}
