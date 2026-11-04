package com.example.omnigo.features.customer.ride.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.promos.data.remote.dto.ApplyVoucherRequestDto
import com.example.omnigo.features.customer.promos.data.remote.dto.ApplyVoucherResponseDto
import com.example.omnigo.features.customer.promos.data.remote.dto.CommitVoucherRequestDto
import com.example.omnigo.features.customer.promos.data.remote.dto.DeliveryFeeRequestDto
import com.example.omnigo.features.customer.promos.data.remote.dto.DeliveryFeeResponseDto
import com.example.omnigo.features.customer.promos.data.remote.dto.VoucherDto
import com.example.omnigo.features.customer.promos.data.remote.dto.VoucherUsageDto
import com.example.omnigo.features.customer.ride.data.remote.dto.PricingRequest
import com.example.omnigo.features.customer.ride.data.remote.dto.PricingResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.RouteRequest
import com.example.omnigo.features.customer.ride.data.remote.dto.RouteResultDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PricingApi {

    @POST(ApiEndpoints.PRICING_CALCULATE)
    suspend fun calculatePricing(
        @Body request: PricingRequest,
        @Query("nearbyDriversCount") nearbyDriversCount: Int = 0
    ): PricingResponse

    @POST(ApiEndpoints.PRICING_ROUTE)
    suspend fun getRoute(
        @Body request: RouteRequest
    ): RouteResultDto

    @POST(ApiEndpoints.PRICING_DELIVERY_FEE)
    suspend fun calculateDeliveryFee(
        @Body request: DeliveryFeeRequestDto
    ): Response<ApiResponse<DeliveryFeeResponseDto>>

    @GET(ApiEndpoints.VOUCHERS)
    suspend fun getVouchers(
        @Query("serviceType") serviceType: String? = null,
        @Query("userId") userId: String? = null
    ): Response<ApiResponse<List<VoucherDto>>>

    @GET(ApiEndpoints.VOUCHERS_APPLICABLE)
    suspend fun getApplicableVouchers(
        @Query("orderAmount") orderAmount: Double,
        @Query("serviceType") serviceType: String = "ALL",
        @Query("userId") userId: String? = null
    ): Response<ApiResponse<List<VoucherDto>>>

    @POST(ApiEndpoints.VOUCHERS_APPLY)
    suspend fun applyVoucher(
        @Body request: ApplyVoucherRequestDto
    ): Response<ApiResponse<ApplyVoucherResponseDto>>

    @POST(ApiEndpoints.VOUCHERS_COMMIT)
    suspend fun commitVoucher(
        @Body request: CommitVoucherRequestDto
    ): Response<ApiResponse<ApplyVoucherResponseDto>>

    @POST(ApiEndpoints.VOUCHERS_ROLLBACK)
    suspend fun rollbackVoucher(
        @Query("code") code: String,
        @Query("userId") userId: String,
        @Query("orderId") orderId: String? = null
    ): Response<ApiResponse<Void>>

    @GET(ApiEndpoints.VOUCHERS_MY_USAGES)
    suspend fun getMyVoucherUsages(
        @Query("userId") userId: String
    ): Response<ApiResponse<List<VoucherUsageDto>>>
}
