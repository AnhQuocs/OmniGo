package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus

fun BookingResponse.toDomain(): Booking {
    val bookingStatus = try {
        status?.let { BookingStatus.valueOf(it.uppercase()) } ?: BookingStatus.PENDING
    } catch (e: IllegalArgumentException) {
        BookingStatus.PENDING
    }

    return Booking(
        bookingId = bookingId ?: 0L,
        customerId = customerId ?: 0L,
        driverId = driverId,
        customerAvatarUrl = customerAvatarUrl,
        driverAvatarUrl = driverAvatarUrl,
        pickupAddress = pickupAddress.orEmpty(),
        dropoffAddress = dropoffAddress.orEmpty(),
        startLongitude = startLongitude ?: 0.0,
        startLatitude = startLatitude ?: 0.0,
        endLongitude = endLongitude ?: 0.0,
        endLatitude = endLatitude ?: 0.0,
        status = bookingStatus,
        distanceInKm = distanceInKm ?: 0.0,
        price = price ?: 0.0,
        vehicleType = vehicleType,
        paymentMethod = paymentMethod,
        createdAt = createdAt,
        completedAt = completedAt
    )
}
