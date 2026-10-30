package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.driver.domain.model.RideOfferEvent

fun BookingResponse.toRideOfferEvent(): RideOfferEvent? {
    val id = bookingId ?: return null
    if (id <= 0L) return null

    val bookingStatus = try {
        if (!status.isNullOrBlank()) {
            BookingStatus.valueOf(status.uppercase())
        } else {
            BookingStatus.PENDING
        }
    } catch (_: Exception) {
        BookingStatus.PENDING
    }

    if (bookingStatus == BookingStatus.CANCELLED) {
        return RideOfferEvent(
            bookingId = id,
            pickupLat = startLatitude ?: 0.0,
            pickupLng = startLongitude ?: 0.0,
            dropoffLat = endLatitude ?: 0.0,
            dropoffLng = endLongitude ?: 0.0,
            pickupAddress = pickupAddress?.trim().orEmpty(),
            dropoffAddress = dropoffAddress?.trim().orEmpty(),
            distanceInKm = distanceInKm ?: 0.0,
            price = price ?: 0.0,
            paymentMethod = paymentMethod,
            status = BookingStatus.CANCELLED,
            customerId = customerId
        )
    }

    val sLat = startLatitude ?: return null
    val sLng = startLongitude ?: return null
    val eLat = endLatitude ?: return null
    val eLng = endLongitude ?: return null

    if (sLat !in -90.0..90.0 || sLng !in -180.0..180.0 ||
        eLat !in -90.0..90.0 || eLng !in -180.0..180.0
    ) {
        return null
    }

    return RideOfferEvent(
        bookingId = id,
        pickupLat = sLat,
        pickupLng = sLng,
        dropoffLat = eLat,
        dropoffLng = eLng,
        pickupAddress = pickupAddress?.trim().orEmpty(),
        dropoffAddress = dropoffAddress?.trim().orEmpty(),
        distanceInKm = (distanceInKm ?: 0.0).coerceAtLeast(0.0),
        price = (price ?: 0.0).coerceAtLeast(0.0),
        paymentMethod = paymentMethod,
        status = bookingStatus,
        customerName = customerName?.trim()?.takeIf { it.isNotBlank() },
        customerPhone = customerPhone?.trim()?.takeIf { it.isNotBlank() },
        customerId = customerId
    )
}
