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
    const val RESTAURANT_DETAIL = "$API_V1/restaurants/{id}"
    const val RESTAURANT_ITEMS = "$API_V1/restaurants/{id}/items"
    const val FOOD_ORDERS = "$API_V1/food-orders"
    const val FOOD_ORDER_DETAIL = "$API_V1/food-orders/{orderId}"
    const val FOOD_ORDERS_MY = "$API_V1/food-orders/my-orders"
    const val FOOD_ORDER_CANCEL = "$API_V1/food-orders/{orderId}/cancel"
    const val FOOD_ORDER_SWITCH_TO_CASH = "$API_V1/food-orders/{orderId}/switch-to-cash"
    const val FOOD_ORDER_RETRY_DRIVER = "$API_V1/food-orders/{orderId}/retry-driver"
    const val DRIVER_PROFILE = "drivers/{id}"

    // --- DRIVER & LOCATION ---
    const val DRIVER_STATUS = "$API_V1/drivers/{driverId}/status"
    const val DRIVER_STATUS_ME = "$API_V1/drivers/me/status"
    const val DRIVER_PROFILE_BY_ID = "$API_V1/drivers/{driverId}/profile"
    const val DRIVER_PROFILE_ME = "$API_V1/drivers/me/profile"
    const val DRIVER_LOCATION_ME = "$API_V1/locations/drivers/me"
}