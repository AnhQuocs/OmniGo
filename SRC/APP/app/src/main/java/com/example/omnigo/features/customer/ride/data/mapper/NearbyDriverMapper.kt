package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.DriverLocationResponse
import com.example.omnigo.features.customer.ride.domain.model.NearbyDriver

fun DriverLocationResponse.toDomain(): NearbyDriver? {
    val id = driverId ?: return null
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return NearbyDriver(
        driverId = id,
        latitude = lat,
        longitude = lng,
        distanceInKm = distanceInKm ?: 0.0
    )
}

fun List<DriverLocationResponse>?.toDomain(): List<NearbyDriver> {
    return this?.mapNotNull { it.toDomain() } ?: emptyList()
}
