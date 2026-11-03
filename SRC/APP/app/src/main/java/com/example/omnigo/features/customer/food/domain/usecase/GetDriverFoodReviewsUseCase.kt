package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.DriverFoodReviewsSummary
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import javax.inject.Inject

class GetDriverFoodReviewsUseCase @Inject constructor(
    private val foodReviewRepository: FoodReviewRepository
) {
    suspend operator fun invoke(driverId: Long): Result<DriverFoodReviewsSummary> {
        return foodReviewRepository.getDriverFoodReviews(driverId)
    }
}
