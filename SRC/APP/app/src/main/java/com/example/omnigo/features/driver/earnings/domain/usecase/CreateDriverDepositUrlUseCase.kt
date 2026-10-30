package com.example.omnigo.features.driver.earnings.domain.usecase

import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import javax.inject.Inject

class CreateDriverDepositUrlUseCase @Inject constructor(
    private val repository: DriverPaymentRepository
) {
    suspend operator fun invoke(method: String, amount: Double): Result<String> {
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Số tiền nạp phải lớn hơn 0"))
        }
        val cleanMethod = if (method.isBlank()) "VNPAY" else method.uppercase()
        return repository.createDepositPaymentUrl(cleanMethod, amount)
    }
}

