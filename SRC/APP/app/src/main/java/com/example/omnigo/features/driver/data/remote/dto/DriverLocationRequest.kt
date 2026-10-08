package com.example.omnigo.features.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverLocationRequest(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("bearing")
    val bearing: Float? = null,
    @SerializedName("speed")
    val speed: Float? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null
)
