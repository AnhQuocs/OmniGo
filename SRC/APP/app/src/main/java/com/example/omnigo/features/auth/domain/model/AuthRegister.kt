package com.example.omnigo.features.auth.domain.model

data class RegisterUser(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
)

data class RegisterDriver(
    val user: RegisterUser,
    val vehicleType: VehicleType,
    val licensePlate: String,
    val vehicleModel: String,
    val cccdNumber: String = "",
    val cccdFrontImage: String = "",
    val cccdBackImage: String = "",
    val gplxNumber: String = "",
    val gplxFrontImage: String = "",
    val gplxBackImage: String = ""
)