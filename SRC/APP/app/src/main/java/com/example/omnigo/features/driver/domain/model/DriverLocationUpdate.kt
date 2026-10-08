package com.example.omnigo.features.driver.domain.model

data class DriverLocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float? = null,
    val speed: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
