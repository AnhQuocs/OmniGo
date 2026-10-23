package com.example.omnigo.features.customer.ride.domain.model

data class RideStatusEvent(
    val bookingId: Long,
    val status: BookingStatus,
    val driverId: Long? = null,
    val price: Double? = null,
    val paymentMethod: String? = null
)
