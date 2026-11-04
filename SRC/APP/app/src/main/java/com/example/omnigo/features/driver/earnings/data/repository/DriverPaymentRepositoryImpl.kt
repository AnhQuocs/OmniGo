package com.example.omnigo.features.driver.earnings.data.repository

import com.example.omnigo.core.network.idempotency.IdempotencyKeyManager
import com.example.omnigo.features.driver.earnings.data.mapper.DriverPaymentMapper
import com.example.omnigo.features.driver.earnings.data.remote.api.DriverPaymentApi
import com.example.omnigo.features.driver.earnings.data.remote.dto.PaymentStatusResponseDto
import com.example.omnigo.features.driver.earnings.domain.model.DriverWallet
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverPaymentRepositoryImpl @Inject constructor(
    private val paymentApi: DriverPaymentApi,
    private val idempotencyKeyManager: IdempotencyKeyManager
) : DriverPaymentRepository {

    override suspend fun withdraw(amount: Double): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = paymentApi.withdraw(amount = amount)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val err = response.errorBody()?.string() ?: "Rút tiền thất bại"
                    Result.failure(Exception(err))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createDepositPaymentUrl(method: String, amount: Double): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fingerprint = "DRIVER_DEPOSIT_${method}_${amount.toLong()}"
                val key = idempotencyKeyManager.getOrCreateKey(IdempotencyKeyManager.SCOPE_PAYMENT_CREATE, fingerprint)
                val response = paymentApi.createPayment(
                    idempotencyKey = key,
                    method = method,
                    type = "DRIVER_DEPOSIT",
                    amount = amount
                )
                val url = response.paymentUrl
                if (!url.isNullOrBlank()) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("Không lấy được đường dẫn thanh toán"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun depositCustomerWallet(amount: Double, method: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fingerprint = "CUSTOMER_DEPOSIT_${method}_${amount.toLong()}"
                val key = idempotencyKeyManager.getOrCreateKey(IdempotencyKeyManager.SCOPE_PAYMENT_CREATE, fingerprint)
                val response = paymentApi.createPayment(
                    idempotencyKey = key,
                    method = method,
                    type = "CUSTOMER_DEPOSIT",
                    amount = amount
                )
                val url = response.paymentUrl
                if (!url.isNullOrBlank()) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("Không lấy được đường dẫn thanh toán"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createOrderPayment(
        bookingId: Long,
        amount: Double,
        method: String,
        type: String
    ): Result<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val fingerprint = "${type}_${bookingId}_${method}_${amount.toLong()}"
                val key = idempotencyKeyManager.getOrCreateKey(IdempotencyKeyManager.SCOPE_PAYMENT_CREATE, fingerprint)
                val response = paymentApi.createPayment(
                    idempotencyKey = key,
                    method = method,
                    type = type,
                    amount = amount,
                    bookingId = bookingId
                )
                Result.success(response.paymentUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getWallet(): Result<DriverWallet> {
        return withContext(Dispatchers.IO) {
            try {
                val response = paymentApi.getMyWallet()
                Result.success(DriverPaymentMapper.mapToDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getTransactions(): Result<List<DriverWalletTransaction>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = paymentApi.getTransactions()
                val list = response.map { DriverPaymentMapper.mapToDomain(it) }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getBookingPaymentStatus(bookingId: Long): Result<PaymentStatusResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val responseBody = paymentApi.getBookingPaymentStatus(bookingId)
                val raw = responseBody.string()
                Result.success(parsePaymentStatus(raw, bookingId, isBooking = true))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getFoodOrderPaymentStatus(orderId: Long): Result<PaymentStatusResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val responseBody = paymentApi.getFoodOrderPaymentStatus(orderId)
                val raw = responseBody.string()
                Result.success(parsePaymentStatus(raw, orderId, isBooking = false))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getPlatformFeePercentage(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val res = paymentApi.getPlatformFee()
                Result.success(res.percentage ?: 20)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parsePaymentStatus(rawBody: String, id: Long, isBooking: Boolean): PaymentStatusResponseDto {
        val clean = rawBody.trim().replace("\"", "")
        val isSuccess = clean.equals("SUCCESS", ignoreCase = true) ||
            clean.contains("paid:true", ignoreCase = true) ||
            clean.contains("paymentStatus:SUCCESS", ignoreCase = true)

        return PaymentStatusResponseDto(
            bookingId = if (isBooking) id else null,
            orderId = if (!isBooking) id else null,
            paid = isSuccess,
            paymentStatus = if (isSuccess) "SUCCESS" else "PENDING"
        )
    }
}
