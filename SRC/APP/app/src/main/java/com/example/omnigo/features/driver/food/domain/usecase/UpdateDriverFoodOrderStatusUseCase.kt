package com.example.omnigo.features.driver.food.domain.usecase

import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import javax.inject.Inject

class UpdateDriverFoodOrderStatusUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(
        orderId: Long,
        status: String,
        reason: String? = null,
        reasonCode: String? = null
    ): Result<DriverDeliveryOrder> {
        if (orderId <= 0L) {
            return Result.failure(IllegalArgumentException("Invalid orderId: $orderId"))
        }
        val normalizedStatus = status.trim().uppercase()
        if (normalizedStatus !in VALID_STATUSES) {
            return Result.failure(IllegalArgumentException("Invalid status: $status"))
        }
        return driverRepository.updateDriverOrderStatus(orderId, normalizedStatus, reason, reasonCode)
    }

    companion object {
        val VALID_STATUSES = setOf("DELIVERING", "COMPLETED", "CANCELLED")
    }
}
