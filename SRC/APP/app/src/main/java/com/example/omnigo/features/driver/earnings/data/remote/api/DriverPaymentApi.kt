package com.example.omnigo.features.driver.earnings.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.features.driver.earnings.data.remote.dto.PaymentUrlResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.TransactionResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.WalletResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DriverPaymentApi {

    @POST(ApiEndpoints.DRIVER_WITHDRAW)
    suspend fun withdraw(
        @Query("amount") amount: Double
    ): Response<Unit>

    @POST(ApiEndpoints.PAYMENT_CREATE)
    suspend fun createPayment(
        @Query("method") method: String,
        @Query("type") type: String = "DRIVER_DEPOSIT",
        @Query("amount") amount: Double
    ): PaymentUrlResponseDto

    @GET(ApiEndpoints.PAYMENTS_WALLET)
    suspend fun getMyWallet(): WalletResponseDto

    @GET(ApiEndpoints.PAYMENTS_TRANSACTIONS)
    suspend fun getTransactions(): List<TransactionResponseDto>
}

