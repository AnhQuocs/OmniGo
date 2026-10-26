package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RouteRequest(
    @SerializedName("startLatitude")
    val startLatitude: Double,
    @SerializedName("startLongitude")
    val startLongitude: Double,
    @SerializedName("endLatitude")
    val endLatitude: Double,
    @SerializedName("endLongitude")
    val endLongitude: Double,
    @SerializedName("vehicleType")
    val vehicleType: String = "BIKE"
)
