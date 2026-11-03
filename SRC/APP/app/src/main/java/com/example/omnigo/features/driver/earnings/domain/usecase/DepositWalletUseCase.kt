package com.example.omnigo.features.driver.earnings.domain.usecase

import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import javax.inject.Inject

class DepositWalletUseCase @Inject constructor(
    private val repository: DriverPaymentRepository
) {
    suspend operator fun invoke(amount: Double, method: String = "VNPAY"): Result<String> {
        return repository.depositCustomerWallet(amount = amount, method = method)
    }
}
