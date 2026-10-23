package com.example.omnigo.features.customer.ride.domain.model

data class Booking(
    val bookingId: Long,
    val customerId: Long,
    val driverId: Long?,
    val customerAvatarUrl: String?,
    val driverAvatarUrl: String?,
    val startLongitude: Double,
    val startLatitude: Double,
    val endLongitude: Double,
    val endLatitude: Double,
    val status: BookingStatus,
    val distanceInKm: Double,
    val price: Double,
    val paymentMethod: String?
)
