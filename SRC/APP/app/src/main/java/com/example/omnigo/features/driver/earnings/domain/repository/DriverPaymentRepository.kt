package com.example.omnigo.features.driver.earnings.domain.repository

import com.example.omnigo.features.driver.earnings.data.remote.dto.PaymentStatusResponseDto
import com.example.omnigo.features.driver.earnings.domain.model.DriverWallet
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction

interface DriverPaymentRepository {
    suspend fun withdraw(amount: Double): Result<Unit>
    suspend fun createDepositPaymentUrl(method: String, amount: Double): Result<String>
    suspend fun depositCustomerWallet(amount: Double, method: String = "VNPAY"): Result<String>
    suspend fun createOrderPayment(
        bookingId: Long,
        amount: Double,
        method: String,
        type: String = "FOOD_PAYMENT"
    ): Result<String?>
    suspend fun getWallet(): Result<DriverWallet>
    suspend fun getTransactions(): Result<List<DriverWalletTransaction>>
    suspend fun getBookingPaymentStatus(bookingId: Long): Result<PaymentStatusResponseDto>
    suspend fun getFoodOrderPaymentStatus(orderId: Long): Result<PaymentStatusResponseDto>
}
