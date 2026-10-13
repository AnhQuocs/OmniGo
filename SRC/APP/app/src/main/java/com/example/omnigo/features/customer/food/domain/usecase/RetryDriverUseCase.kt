package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.RetryDriverResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class RetryDriverUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(orderId: Long): RetryDriverResult {
        if (orderId <= 0) {
            return RetryDriverResult.Error(
                error = FoodError.UNKNOWN_ERROR,
                message = "Invalid order ID"
            )
        }
        return repository.retryDriver(orderId)
    }
}
