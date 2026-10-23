package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import javax.inject.Inject

class GetFoodReviewByOrderUseCase @Inject constructor(
    private val foodReviewRepository: FoodReviewRepository
) {
    suspend operator fun invoke(orderId: Long): Result<FoodReview?> {
        if (orderId <= 0) {
            return Result.failure(IllegalArgumentException("Invalid orderId"))
        }
        return foodReviewRepository.getFoodReviewByOrderId(orderId)
    }
}
