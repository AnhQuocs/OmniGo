package com.example.omnigo.features.driver.food.domain.usecase

import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import javax.inject.Inject

class GetDriverActiveFoodOrdersUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(): Result<List<DriverDeliveryOrder>> {
        return driverRepository.getDriverActiveFoodOrders()
    }
}
