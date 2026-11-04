package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus

fun BookingResponse.toDomain(): Booking {
    val bookingStatus = try {
        BookingStatus.valueOf(status?.uppercase() ?: "PENDING")
    } catch (_: IllegalArgumentException) {
        BookingStatus.PENDING
    }

    val normalizedRate = if (commissionRate != null && commissionRate > 1.0) {
        commissionRate / 100.0
    } else {
        commissionRate
    }

    return Booking(
        bookingId = bookingId ?: 0L,
        customerId = customerId ?: 0L,
        driverId = driverId,
        customerAvatarUrl = customerAvatarUrl,
        driverAvatarUrl = driverAvatarUrl,
        driverName = driverName,
        driverPhone = driverPhone,
        driverVehiclePlate = driverVehiclePlate,
        driverRating = driverRating,
        pickupAddress = pickupAddress.orEmpty(),
        dropoffAddress = dropoffAddress.orEmpty(),
        startLongitude = startLongitude ?: 0.0,
        startLatitude = startLatitude ?: 0.0,
        endLongitude = endLongitude ?: 0.0,
        endLatitude = endLatitude ?: 0.0,
        status = bookingStatus,
        distanceInKm = distanceInKm ?: 0.0,
        price = price ?: 0.0,
        originalPrice = originalAmount,
        discountAmount = discountAmount ?: 0.0,
        voucherCode = voucherCode,
        vehicleType = vehicleType,
        paymentMethod = paymentMethod,
        createdAt = createdAt,
        completedAt = completedAt,
        platformFee = platformFee,
        driverEarnings = driverEarnings,
        commissionRate = normalizedRate
    )
}
