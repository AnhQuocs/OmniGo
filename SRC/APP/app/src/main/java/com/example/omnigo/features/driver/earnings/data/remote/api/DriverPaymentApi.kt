package com.example.omnigo.features.driver.earnings.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.features.driver.earnings.data.remote.dto.PaymentUrlResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.PlatformFeeResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.TransactionResponseDto
import com.example.omnigo.features.driver.earnings.data.remote.dto.WalletResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DriverPaymentApi {

    @POST(ApiEndpoints.DRIVER_WITHDRAW)
    suspend fun withdraw(
        @Query("amount") amount: Double
    ): Response<Unit>

    @Deprecated("POST /api/v1/payments/wallet/deposit bị chặn (410 GONE / 403). Sử dụng createPayment với type CUSTOMER_DEPOSIT hoặc DRIVER_DEPOSIT.")
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
        @Header("Idempotency-Key") idempotencyKey: String,
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
    ): ResponseBody

    @GET(ApiEndpoints.FOOD_ORDER_PAYMENT_STATUS)
    suspend fun getFoodOrderPaymentStatus(
        @Path("orderId") orderId: Long
    ): ResponseBody

    @GET(ApiEndpoints.PLATFORM_FEE)
    suspend fun getPlatformFee(): PlatformFeeResponseDto
}
