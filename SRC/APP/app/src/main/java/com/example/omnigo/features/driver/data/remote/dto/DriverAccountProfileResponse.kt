package com.example.omnigo.features.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverAccountProfileResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName(value = "fullName", alternate = ["driverName", "name"])
    val fullName: String? = null,
    @SerializedName(value = "phoneNumber", alternate = ["driverPhone", "phone"])
    val phoneNumber: String? = null,
    @SerializedName(value = "licensePlate", alternate = ["vehiclePlate", "driverVehiclePlate"])
    val vehiclePlate: String? = null,
    @SerializedName(value = "status", alternate = ["approvalStatus", "driverStatus", "userStatus"])
    val status: String? = null,
    @SerializedName(value = "isOnline", alternate = ["online"])
    val isOnline: Boolean? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)
