package com.example.omnigo.features.auth.data.remote.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String?,
    val user: UserResponse
)
