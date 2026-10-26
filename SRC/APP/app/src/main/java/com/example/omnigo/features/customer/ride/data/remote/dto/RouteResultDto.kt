package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RouteResultDto(
    @SerializedName("distanceKm")
    val distanceKm: Double? = null,
    @SerializedName("durationMinutes")
    val durationMinutes: Int? = null,
    @SerializedName("encodedPolyline")
    val encodedPolyline: String? = null,
    @SerializedName("source")
    val source: String? = null
)
