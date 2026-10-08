package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverProfileResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName(value = "fullName", alternate = ["driverName", "name"])
    val driverName: String? = null,
    @SerializedName(value = "phoneNumber", alternate = ["driverPhone", "phone"])
    val driverPhone: String? = null,
    @SerializedName(value = "licensePlate", alternate = ["vehiclePlate", "driverVehiclePlate"])
    val vehiclePlate: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)
