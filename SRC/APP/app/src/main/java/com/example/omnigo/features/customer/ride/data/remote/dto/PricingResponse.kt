package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PricingResponse(
    @SerializedName("baseFare")
    val baseFare: Double? = null,
    @SerializedName("distanceFare")
    val distanceFare: Double? = null,
    @SerializedName("durationFare")
    val durationFare: Double? = null,
    @SerializedName("surgeMultiplier")
    val surgeMultiplier: Double? = null,
    @SerializedName("surgeLevel")
    val surgeLevel: String? = null,
    @SerializedName("totalPrice")
    val totalPrice: Double? = null,
    @SerializedName("distanceInKm")
    val distanceInKm: Double? = null,
    @SerializedName("estimatedDurationMinutes")
    val estimatedDurationMinutes: Int? = null,
    @SerializedName("h3Index")
    val h3Index: String? = null
)
