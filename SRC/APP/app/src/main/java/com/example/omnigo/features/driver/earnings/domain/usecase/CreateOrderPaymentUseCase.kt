package com.example.omnigo.features.driver.earnings.domain.usecase

import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import javax.inject.Inject

class CreateOrderPaymentUseCase @Inject constructor(
    private val repository: DriverPaymentRepository
) {
    suspend operator fun invoke(
        bookingId: Long,
        amount: Double,
        method: String,
        type: String = "FOOD_PAYMENT"
    ): Result<String?> {
        return repository.createOrderPayment(
            bookingId = bookingId,
            amount = amount,
            method = method,
            type = type
        )
    }
}
