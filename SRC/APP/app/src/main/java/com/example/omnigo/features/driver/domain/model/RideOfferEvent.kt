package com.example.omnigo.features.driver.domain.model

import com.example.omnigo.features.customer.ride.domain.model.BookingStatus

data class RideOfferEvent(
    val bookingId: Long,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val distanceInKm: Double,
    val price: Double,
    val paymentMethod: String? = null,
    val status: BookingStatus = BookingStatus.PENDING,
    val timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS,
    val receivedAtMillis: Long = System.currentTimeMillis()
) {
    val isPending: Boolean
        get() = status == BookingStatus.PENDING

    val isCancelled: Boolean
        get() = status == BookingStatus.CANCELLED

    companion object {
        const val DEFAULT_TIMEOUT_SECONDS = 20
    }
}
