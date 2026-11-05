package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverProfileResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName(value = "fullName", alternate = ["driverName", "name", "username"])
    val driverName: String? = null,
    @SerializedName(value = "phoneNumber", alternate = ["driverPhone", "phone"])
    val driverPhone: String? = null,
    @SerializedName(value = "licensePlate", alternate = ["vehiclePlate", "driverVehiclePlate", "driverLicensePlate", "plateNumber"])
    val vehiclePlate: String? = null,
    @SerializedName(value = "avatarUrl", alternate = ["avatar", "driverAvatarUrl", "profileImageUrl"])
    val avatarUrl: String? = null
)
