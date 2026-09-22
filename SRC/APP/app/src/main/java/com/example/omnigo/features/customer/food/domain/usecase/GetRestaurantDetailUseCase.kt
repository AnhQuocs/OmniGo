package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class GetRestaurantDetailUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(id: Long): GetRestaurantDetailResult {
        return repository.getRestaurantDetail(id)
    }
}
