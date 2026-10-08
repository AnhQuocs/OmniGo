package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.SwitchToCashResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class SwitchToCashUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(orderId: Long): SwitchToCashResult {
        if (orderId <= 0) {
            return SwitchToCashResult.Error(
                error = FoodError.UNKNOWN_ERROR,
                message = "Invalid order ID"
            )
        }
        return repository.switchToCash(orderId)
    }
}
