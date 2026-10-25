package com.example.omnigo.features.customer.ride.data.remote.dto

import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.google.gson.annotations.SerializedName

data class DriverLiveLocationDto(
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("bearing")
    val bearing: Float? = null,
    @SerializedName("speed")
    val speed: Float? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null
) {
    fun toDomain(): DriverLiveLocation? {
        val validDriverId = driverId?.takeIf { it > 0L } ?: return null
        val lat = latitude ?: return null
        val lng = longitude ?: return null
        return DriverLiveLocation(
            driverId = validDriverId,
            latitude = lat,
            longitude = lng,
            bearing = bearing,
            speed = speed,
            timestamp = timestamp ?: System.currentTimeMillis()
        )
    }
}
