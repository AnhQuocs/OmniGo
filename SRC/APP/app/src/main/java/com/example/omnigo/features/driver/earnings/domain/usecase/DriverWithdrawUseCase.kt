package com.example.omnigo.features.driver.earnings.domain.usecase

import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import javax.inject.Inject

class DriverWithdrawUseCase @Inject constructor(
    private val repository: DriverPaymentRepository
) {
    suspend operator fun invoke(amount: Double): Result<Unit> {
        if (amount < 50_000.0) {
            return Result.failure(IllegalArgumentException("Số tiền rút tối thiểu là 50.000 đ"))
        }
        return repository.withdraw(amount)
    }
}

