package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CancelFoodOrderResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class CancelFoodOrderUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(
        orderId: Long,
        reasonCode: String? = null,
        reason: String? = null
    ): CancelFoodOrderResult {
        if (orderId <= 0) {
            return CancelFoodOrderResult.Error(
                error = FoodError.UNKNOWN_ERROR,
                message = "Invalid order ID"
            )
        }
        return repository.cancelFoodOrder(orderId, reasonCode, reason)
    }
}
