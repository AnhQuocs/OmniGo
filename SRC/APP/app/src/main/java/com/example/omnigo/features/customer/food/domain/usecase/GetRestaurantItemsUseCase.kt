package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class GetRestaurantItemsUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(restaurantId: Long, availableOnly: Boolean = false): GetMenuItemsResult {
        return repository.getRestaurantItems(restaurantId, availableOnly)
    }
}
