package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class GetRestaurantsUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(search: String? = null): GetRestaurantsResult {
        return repository.getRestaurants(search)
    }
}
