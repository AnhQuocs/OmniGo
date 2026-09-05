package com.example.omnigo.features.auth.data.remote.dto.request

data class DriverRegisterRequest(
    val email: String?,
    val password: String,
    val fullName: String,
    val phoneNumber: String?,
    val firebaseToken: String?,
    val vehicleType: String,
    val licensePlate: String,
    val vehicleModel: String
)