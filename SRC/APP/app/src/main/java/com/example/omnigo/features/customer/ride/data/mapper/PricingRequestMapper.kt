package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.PricingRequest

fun toPricingRequest(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double
): PricingRequest {
    return PricingRequest(
        startLongitude = startLongitude,
        startLatitude = startLatitude,
        endLongitude = endLongitude,
        endLatitude = endLatitude
    )
}
