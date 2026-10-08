package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.DriverProfileResponse
import com.example.omnigo.features.customer.food.domain.model.DriverProfile

fun DriverProfileResponse.toDomain(requestedDriverId: Long): DriverProfile =
    DriverProfile(
        driverId = requestedDriverId,
        driverName = driverName,
        driverPhone = driverPhone,
        vehiclePlate = vehiclePlate,
        avatarUrl = avatarUrl
    )
