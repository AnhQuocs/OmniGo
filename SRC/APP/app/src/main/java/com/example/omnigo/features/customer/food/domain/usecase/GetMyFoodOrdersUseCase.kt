package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class GetMyFoodOrdersUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(): GetMyFoodOrdersResult {
        return repository.getMyFoodOrders()
    }
}
