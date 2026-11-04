package com.example.omnigo.features.driver.earnings.domain.usecase

import com.example.omnigo.features.driver.earnings.data.remote.dto.PaymentStatusResponseDto
import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import javax.inject.Inject

class GetFoodOrderPaymentStatusUseCase @Inject constructor(
    private val repository: DriverPaymentRepository
) {
    suspend operator fun invoke(orderId: Long): Result<PaymentStatusResponseDto> {
        return repository.getFoodOrderPaymentStatus(orderId)
    }
}
