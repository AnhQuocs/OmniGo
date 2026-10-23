package com.example.omnigo.features.driver.food.domain.usecase

import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import javax.inject.Inject

class GetDriverFoodOrderDetailUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(orderId: Long): Result<DriverDeliveryOrder> {
        if (orderId <= 0L) {
            return Result.failure(IllegalArgumentException("Invalid orderId: $orderId"))
        }
        return driverRepository.getDriverFoodOrderDetail(orderId)
    }
}
