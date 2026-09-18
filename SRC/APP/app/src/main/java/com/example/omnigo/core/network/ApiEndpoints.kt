package com.example.omnigo.core.network

object ApiEndpoints {
    private const val API_V1 = "api/v1"

    // --- AUTHENTICATION ---
    const val LOGIN = "$API_V1/auth/login"
    const val REFRESH_TOKEN = "$API_V1/auth/refresh"
    const val LOGOUT = "$API_V1/auth/logout"
    const val REGISTER_CUSTOMER = "$API_V1/users/register/customer"
    const val REGISTER_DRIVER = "$API_V1/drivers/register"

    const val GET_ME = "$API_V1/users/me"

    // --- FOOD / RESTAURANTS ---
    const val RESTAURANTS = "$API_V1/restaurants"
}