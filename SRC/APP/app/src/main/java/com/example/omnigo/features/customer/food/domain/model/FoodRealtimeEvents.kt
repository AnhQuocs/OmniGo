package com.example.omnigo.features.customer.food.domain.model

data class FoodOrderStatusEvent(
    val orderId: Long,
    val status: String?,
    val timestamp: String?
)

data class FoodOrderAssignmentEvent(
    val orderId: Long,
    val driverId: Long,
    val driverName: String?,
    val driverPhone: String?,
    val vehiclePlate: String?
)

data class DriverLocation(
    val driverId: Long,
    val latitude: Double,
    val longitude: Double,
    val bearing: Double?,
    val speed: Double?,
    val timestamp: String?
)
