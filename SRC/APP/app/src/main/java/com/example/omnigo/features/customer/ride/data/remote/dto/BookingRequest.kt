package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookingRequest(
    @SerializedName("startLongitude")
    val startLongitude: Double,
    @SerializedName("startLatitude")
    val startLatitude: Double,
    @SerializedName("endLongitude")
    val endLongitude: Double,
    @SerializedName("endLatitude")
    val endLatitude: Double,
    @SerializedName("vehicleType")
    val vehicleType: String? = null,
    @SerializedName("pickupAddress")
    val pickupAddress: String? = null,
    @SerializedName("dropoffAddress")
    val dropoffAddress: String? = null
)
