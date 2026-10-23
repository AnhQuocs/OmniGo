package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.PricingResponse
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate

fun PricingResponse.toDomain(): PricingEstimate {
    return PricingEstimate(
        baseFare = baseFare ?: 0.0,
        distanceFare = distanceFare ?: 0.0,
        durationFare = durationFare ?: 0.0,
        surgeMultiplier = surgeMultiplier ?: 1.0,
        surgeLevel = surgeLevel,
        totalPrice = totalPrice ?: 0.0,
        distanceInKm = distanceInKm ?: 0.0,
        estimatedDurationMinutes = estimatedDurationMinutes ?: 0,
        h3Index = h3Index
    )
}
