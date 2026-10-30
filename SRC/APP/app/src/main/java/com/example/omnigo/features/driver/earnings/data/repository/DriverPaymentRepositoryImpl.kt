package com.example.omnigo.features.driver.earnings.data.repository

import com.example.omnigo.features.driver.earnings.data.mapper.DriverPaymentMapper
import com.example.omnigo.features.driver.earnings.data.remote.api.DriverPaymentApi
import com.example.omnigo.features.driver.earnings.domain.model.DriverWallet
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverPaymentRepositoryImpl @Inject constructor(
    private val paymentApi: DriverPaymentApi
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
                val response = paymentApi.createPayment(
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
}

