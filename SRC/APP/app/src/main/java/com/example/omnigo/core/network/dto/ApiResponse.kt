package com.example.omnigo.core.network.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val timestamp: String?
)
