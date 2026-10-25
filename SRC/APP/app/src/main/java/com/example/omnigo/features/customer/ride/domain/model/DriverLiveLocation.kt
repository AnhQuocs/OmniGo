package com.example.omnigo.features.customer.ride.domain.model

data class DriverLiveLocation(
    val driverId: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bearing: Float? = null,
    val speed: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
