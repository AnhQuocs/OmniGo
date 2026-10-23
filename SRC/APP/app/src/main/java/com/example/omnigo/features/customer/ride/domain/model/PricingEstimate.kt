package com.example.omnigo.features.customer.ride.domain.model

data class PricingEstimate(
    val baseFare: Double,
    val distanceFare: Double,
    val durationFare: Double,
    val surgeMultiplier: Double,
    val surgeLevel: String?,
    val totalPrice: Double,
    val distanceInKm: Double,
    val estimatedDurationMinutes: Int,
    val h3Index: String?
)
