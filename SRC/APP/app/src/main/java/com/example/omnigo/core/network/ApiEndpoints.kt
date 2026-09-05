package com.example.omnigo.core.network

object ApiEndpoints {
    private const val API_V1 = "api/v1"

    // --- AUTHENTICATION ---
    const val REGISTER_CUSTOMER = "$API_V1/users/register/customer"
    const val DRIVER_CUSTOMER = "$API_V1/drivers/register"
    const val LOGIN = "$API_V1/auth/login"
    const val REFRESH_TOKEN = "$API_V1/auth/refresh"
    const val LOGOUT = "$API_V1/auth/logout"
}