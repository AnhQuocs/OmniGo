package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PricingRequest(
    @SerializedName("startLongitude")
    val startLongitude: Double,
    @SerializedName("startLatitude")
    val startLatitude: Double,
    @SerializedName("endLongitude")
    val endLongitude: Double,
    @SerializedName("endLatitude")
    val endLatitude: Double
)
