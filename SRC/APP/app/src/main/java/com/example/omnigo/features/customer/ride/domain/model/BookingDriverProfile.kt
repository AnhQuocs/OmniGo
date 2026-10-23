package com.example.omnigo.features.customer.ride.domain.model

data class BookingDriverProfile(
    val driverId: Long,
    val driverName: String?,
    val driverPhone: String?,
    val vehiclePlate: String?,
    val vehicleType: String?,
    val vehicleModel: String?,
    val avatarUrl: String?
)
