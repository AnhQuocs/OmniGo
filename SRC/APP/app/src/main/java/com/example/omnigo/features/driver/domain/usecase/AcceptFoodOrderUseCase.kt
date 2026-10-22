package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.repository.DriverRepository
import javax.inject.Inject

class AcceptFoodOrderUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(orderId: Long): Result<Boolean> {
        if (orderId <= 0L) {
            return Result.failure(IllegalArgumentException("Invalid orderId: $orderId"))
        }
        return driverRepository.acceptFoodOrder(orderId)
    }
}
