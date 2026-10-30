package com.example.omnigo.features.driver.earnings.domain.repository

import com.example.omnigo.features.driver.earnings.domain.model.DriverWallet
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction

interface DriverPaymentRepository {
    suspend fun withdraw(amount: Double): Result<Unit>
    suspend fun createDepositPaymentUrl(method: String, amount: Double): Result<String>
    suspend fun getWallet(): Result<DriverWallet>
    suspend fun getTransactions(): Result<List<DriverWalletTransaction>>
}

