package com.example.omnigo.features.driver.earnings.domain.usecase

import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import javax.inject.Inject

class GetDriverTransactionsUseCase @Inject constructor(
    private val repository: DriverPaymentRepository
) {
    suspend operator fun invoke(): Result<List<DriverWalletTransaction>> {
        return repository.getTransactions()
    }
}

