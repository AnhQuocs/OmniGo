package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.data.remote.dto.DriverLocationResponse
import com.example.omnigo.features.customer.ride.domain.model.NearbyDriver
import kotlin.math.abs

fun DriverLocationResponse.toDomain(): NearbyDriver? {
    val id = driverId ?: return null
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    val type = when (vehicleType?.uppercase()?.trim()) {
        "BIKE", "OMNIBIKE" -> VehicleType.BIKE
        "CAR_4_SEAT", "CAR4", "OMNICAR4" -> VehicleType.CAR_4_SEAT
        "CAR_7_SEAT", "CAR7", "OMNICAR7" -> VehicleType.CAR_7_SEAT
        else -> when (abs(id % 3).toInt()) {
            0 -> VehicleType.BIKE
            1 -> VehicleType.CAR_4_SEAT
            else -> VehicleType.CAR_7_SEAT
        }
    }
    return NearbyDriver(
        driverId = id,
        latitude = lat,
        longitude = lng,
        distanceInKm = distanceInKm ?: 0.0,
        vehicleType = type
    )
}

fun List<DriverLocationResponse>?.toDomain(): List<NearbyDriver> {
    return this?.mapNotNull { it.toDomain() } ?: emptyList()
}
