package com.example.omnigo.features.driver.earnings.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.features.driver.earnings.data.remote.dto.PaymentStatusResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.PaymentUrlResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.TransactionResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.WalletResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DriverPaymentApi {

    @POST(ApiEndpoints.DRIVER_WITHDRAW)
    suspend fun withdraw(
        @Query("amount") amount: Double
    ): Response<Unit>

    @POST(ApiEndpoints.WALLET_DEPOSIT)
    suspend fun depositWallet(
        @Query("amount") amount: Double,
        @Query("method") method: String = "VNPAY"
    ): PaymentUrlResponseDto

    @POST(ApiEndpoints.WALLET_WITHDRAW)
    suspend fun withdrawWallet(
        @Query("amount") amount: Double
    ): Response<Unit>

    @POST(ApiEndpoints.PAYMENT_CREATE)
    suspend fun createPayment(
        @Query("method") method: String,
        @Query("type") type: String = "DRIVER_DEPOSIT",
        @Query("amount") amount: Double,
        @Query("bookingId") bookingId: Long? = null
    ): PaymentUrlResponseDto

    @GET(ApiEndpoints.PAYMENTS_WALLET)
    suspend fun getMyWallet(): WalletResponseDto

    @GET(ApiEndpoints.PAYMENTS_TRANSACTIONS)
    suspend fun getTransactions(): List<TransactionResponseDto>

    @GET(ApiEndpoints.BOOKING_PAYMENT_STATUS)
    suspend fun getBookingPaymentStatus(
        @Path("bookingId") bookingId: Long
    ): PaymentStatusResponseDto

    @GET(ApiEndpoints.FOOD_ORDER_PAYMENT_STATUS)
    suspend fun getFoodOrderPaymentStatus(
        @Path("orderId") orderId: Long
    ): PaymentStatusResponseDto
}
