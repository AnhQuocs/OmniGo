package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodOrderStatusEventDto(
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
)

data class FoodOrderAssignmentEventDto(
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("driverName")
    val driverName: String? = null,
    @SerializedName("driverPhone")
    val driverPhone: String? = null,
    @SerializedName(value = "vehiclePlate", alternate = ["licensePlate"])
    val vehiclePlate: String? = null
)

data class DriverLocationEventDto(
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("bearing")
    val bearing: Double? = null,
    @SerializedName("speed")
    val speed: Double? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
)
