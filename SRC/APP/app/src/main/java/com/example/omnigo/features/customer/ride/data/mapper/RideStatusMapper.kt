package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent

fun BookingResponse.toRideStatusEvent(): RideStatusEvent? {
    val id = bookingId ?: return null
    if (id <= 0L) return null

    val rawStatus = status?.trim()
    if (rawStatus.isNullOrBlank()) return null

    val bookingStatus = try {
        BookingStatus.valueOf(rawStatus.uppercase())
    } catch (_: Exception) {
        return null
    }

    return RideStatusEvent(
        bookingId = id,
        status = bookingStatus,
        driverId = driverId,
        price = price,
        paymentMethod = paymentMethod
    )
}
