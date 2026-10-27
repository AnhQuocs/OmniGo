package com.example.omnigo.features.customer.ride.domain.model

import com.example.omnigo.features.auth.domain.model.VehicleType

data class NearbyDriver(
    val driverId: Long,
    val longitude: Double,
    val latitude: Double,
    val distanceInKm: Double,
    val vehicleType: VehicleType? = null
)
