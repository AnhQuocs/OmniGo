package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverLocationResponse(
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("distanceInKm")
    val distanceInKm: Double? = null,
    @SerializedName("vehicleType")
    val vehicleType: String? = null
)
