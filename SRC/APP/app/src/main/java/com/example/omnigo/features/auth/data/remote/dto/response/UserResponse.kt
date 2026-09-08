package com.example.omnigo.features.auth.data.remote.dto.response

data class UserResponse(
    val id: Long = 0L,
    val phoneNumber: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val status: String? = null,
    val vehicleType: String? = null,
    val licensePlate: String? = null,
    val vehicleModel: String? = null,
    val createdAt: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null
)