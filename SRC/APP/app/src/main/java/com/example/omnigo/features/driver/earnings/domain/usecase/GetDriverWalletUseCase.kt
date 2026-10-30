package com.example.omnigo.features.driver.earnings.domain.usecase

import com.example.omnigo.features.driver.earnings.domain.model.DriverWallet
import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import javax.inject.Inject

class GetDriverWalletUseCase @Inject constructor(
    private val repository: DriverPaymentRepository
) {
    suspend operator fun invoke(): Result<DriverWallet> {
        return repository.getWallet()
    }
}

