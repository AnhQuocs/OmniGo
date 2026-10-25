package com.example.omnigo.features.driver.domain.model

data class DriverDailyReport(
    val totalTrips: Int = 0,
    val totalRevenue: Double = 0.0,
    val driverEarnings: Double = 0.0,
    val platformFee: Double = 0.0,
    val cashCollected: Double = 0.0,
    val appPaid: Double = 0.0
)
