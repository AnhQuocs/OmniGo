package com.example.omnigo.features.auth.data.remote.dto.response

data class UserResponse(
    val id: Long,
    val phoneNumber: String,
    val email: String?,
    val fullName: String,
    val role: String,
    val status: String?,
    val vehicleType: String?,
    val licensePlate: String?,
    val vehicleModel: String?,
    val createdAt: String?,
    val accessToken: String? = null,
    val refreshToken: String? = null
)