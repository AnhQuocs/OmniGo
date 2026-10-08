package com.example.omnigo.features.customer.food.domain.model

data class FoodOrderTrackingState(
    val orderId: Long,
    val status: String?,
    val driverProfile: DriverProfile?,
    val driverLocation: DriverLocation?
)
