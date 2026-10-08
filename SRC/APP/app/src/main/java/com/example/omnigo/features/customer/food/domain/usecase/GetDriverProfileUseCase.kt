package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class GetDriverProfileUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(driverId: Long): GetDriverProfileResult {
        return foodRepository.getDriverProfile(driverId)
    }
}
