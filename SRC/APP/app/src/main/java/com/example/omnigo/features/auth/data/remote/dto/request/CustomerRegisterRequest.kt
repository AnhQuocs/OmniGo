package com.example.omnigo.features.auth.data.remote.dto.request

data class CustomerRegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val firebaseToken: String?
)