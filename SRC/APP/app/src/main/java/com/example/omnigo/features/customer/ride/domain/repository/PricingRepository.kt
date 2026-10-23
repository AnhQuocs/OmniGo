package com.example.omnigo.features.customer.ride.domain.repository

import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult

interface PricingRepository {
    suspend fun calculatePricing(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
        nearbyDriversCount: Int = 0
    ): CalculateRidePricingResult
}
