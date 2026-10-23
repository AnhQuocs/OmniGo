package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingDriverResponse
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile

fun BookingDriverResponse.toDomain(requestedDriverId: Long? = null): BookingDriverProfile {
    return BookingDriverProfile(
        driverId = driverId ?: requestedDriverId ?: 0L,
        driverName = driverName,
        driverPhone = driverPhone,
        vehiclePlate = vehiclePlate,
        vehicleType = vehicleType,
        vehicleModel = vehicleModel,
        avatarUrl = avatarUrl
    )
}
