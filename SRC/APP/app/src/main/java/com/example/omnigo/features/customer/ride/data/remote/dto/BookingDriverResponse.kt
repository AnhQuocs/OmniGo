package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookingDriverResponse(
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("driverName")
    val driverName: String? = null,
    @SerializedName("driverPhone")
    val driverPhone: String? = null,
    @SerializedName("vehiclePlate")
    val vehiclePlate: String? = null,
    @SerializedName("vehicleType")
    val vehicleType: String? = null,
    @SerializedName("vehicleModel")
    val vehicleModel: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)
