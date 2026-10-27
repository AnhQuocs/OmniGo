package com.example.omnigo.features.customer.ride.domain.model

data class Booking(
    val bookingId: Long = 0L,
    val customerId: Long = 0L,
    val driverId: Long? = null,
    val customerAvatarUrl: String? = null,
    val driverAvatarUrl: String? = null,
    val startLongitude: Double = 0.0,
    val startLatitude: Double = 0.0,
    val endLongitude: Double = 0.0,
    val endLatitude: Double = 0.0,
    val status: BookingStatus = BookingStatus.PENDING,
    val distanceInKm: Double = 0.0,
    val price: Double = 0.0,
    val paymentMethod: String? = null,
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val vehicleType: String? = null
)
