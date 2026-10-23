package com.example.omnigo.features.auth.data.remote.dto.request

data class DriverRegisterRequest(
    val email: String?,
    val password: String,
    val fullName: String,
    val phoneNumber: String?,
    val firebaseToken: String?,
    val vehicleType: String,
    val licensePlate: String,
    val vehicleModel: String,
    val cccdNumber: String? = null,
    val cccdFrontImage: String? = null,
    val cccdBackImage: String? = null,
    val gplxNumber: String? = null,
    val gplxFrontImage: String? = null,
    val gplxBackImage: String? = null,
    val autoApprove: Boolean? = false
)