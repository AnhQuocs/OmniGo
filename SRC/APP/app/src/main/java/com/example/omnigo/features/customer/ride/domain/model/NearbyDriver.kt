package com.example.omnigo.features.customer.ride.domain.model

data class NearbyDriver(
    val driverId: Long,
    val longitude: Double,
    val latitude: Double,
    val distanceInKm: Double
)
