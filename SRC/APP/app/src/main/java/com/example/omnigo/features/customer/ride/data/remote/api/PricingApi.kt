package com.example.omnigo.features.customer.ride.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.features.customer.ride.data.remote.dto.PricingRequest
import com.example.omnigo.features.customer.ride.data.remote.dto.PricingResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.RouteRequest
import com.example.omnigo.features.customer.ride.data.remote.dto.RouteResultDto
import retrofit2.http.Body
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
}
