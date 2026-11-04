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
    @SerializedName(value = "vehicleType", alternate = ["driverVehicleType"])
    val vehicleType: String? = null,
    @SerializedName("vehicleBrand")
    val vehicleBrand: String? = null,
    @SerializedName("licenseNumber")
    val licenseNumber: String? = null,
    @SerializedName(value = "status", alternate = ["driverStatus", "userStatus"])
    val status: String? = null,
    @SerializedName("approvalStatus")
    val approvalStatus: String? = null,
    @SerializedName(value = "isOnline", alternate = ["online"])
    val isOnline: Boolean? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("idCardFrontUrl")
    val idCardFrontUrl: String? = null,
    @SerializedName("idCardBackUrl")
    val idCardBackUrl: String? = null,
    @SerializedName("licenseImageUrl")
    val licenseImageUrl: String? = null,
    @SerializedName("vehicleRegImageUrl")
    val vehicleRegImageUrl: String? = null
)

data class DriverUpdateVehicleRequestDto(
    @SerializedName("vehicleType")
    val vehicleType: String,
    @SerializedName("licensePlate")
    val licensePlate: String,
    @SerializedName("vehicleBrand")
    val vehicleBrand: String
)

data class DriverResubmitRequestDto(
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("licenseNumber")
    val licenseNumber: String,
    @SerializedName("vehicleType")
    val vehicleType: String,
    @SerializedName("licensePlate")
    val licensePlate: String,
    @SerializedName("vehicleBrand")
    val vehicleBrand: String,
    @SerializedName("idCardFrontUrl")
    val idCardFrontUrl: String? = null,
    @SerializedName("idCardBackUrl")
    val idCardBackUrl: String? = null,
    @SerializedName("licenseImageUrl")
    val licenseImageUrl: String? = null,
    @SerializedName("vehicleRegImageUrl")
    val vehicleRegImageUrl: String? = null
)
