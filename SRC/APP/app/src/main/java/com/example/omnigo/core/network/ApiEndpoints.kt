package com.example.omnigo.core.network

object ApiEndpoints {
    private const val API_V1 = "api/v1"

    // --- AUTHENTICATION ---
    const val LOGIN = "$API_V1/auth/login"
    const val REFRESH_TOKEN = "$API_V1/auth/refresh"
    const val LOGOUT = "$API_V1/auth/logout"
    const val REGISTER_CUSTOMER = "$API_V1/users/register/customer"
    const val REGISTER_DRIVER = "$API_V1/drivers/register"
    const val DRIVER_UPLOAD_DOCUMENT = "$API_V1/drivers/upload-document"

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

    // --- FOOD REVIEWS ---
    const val FOOD_REVIEW_UPLOAD_IMAGE = "$API_V1/food-reviews/upload-image"
    const val FOOD_REVIEWS = "$API_V1/food-reviews"
    const val FOOD_REVIEW_BY_ORDER = "$API_V1/food-reviews/order/{orderId}"
    const val RESTAURANT_REVIEWS = "$API_V1/restaurants/{restaurantId}/reviews"

    // --- DRIVER & LOCATION ---
    const val DRIVER_STATUS = "$API_V1/drivers/{driverId}/status"
    const val DRIVER_STATUS_ME = "$API_V1/drivers/me/status"
    const val DRIVER_PROFILE_BY_ID = "$API_V1/drivers/{driverId}/profile"
    const val DRIVER_PROFILE_ME = "$API_V1/drivers/me/profile"
    const val DRIVER_LOCATION_ME = "$API_V1/locations/drivers/me"

    // --- BOOKING FOOD ORDERS (DRIVER) ---
    const val BOOKING_FOOD_ORDER_ACCEPT = "$API_V1/bookings/food-orders/{orderId}/accept"
    const val BOOKING_FOOD_ORDER_REJECT = "$API_V1/bookings/food-orders/{orderId}/reject"
    const val FOOD_ORDERS_DRIVER = "$API_V1/food-orders/driver"
    const val FOOD_ORDER_DRIVER_STATUS = "$API_V1/food-orders/{orderId}/driver-status"

    // --- RIDE & PRICING ---
    const val PRICING_CALCULATE = "$API_V1/pricing/calculate"
    const val BOOKINGS = "$API_V1/bookings"
    const val BOOKING_DRIVER = "$API_V1/bookings/{bookingId}/driver"
    const val BOOKING_CANCEL = "$API_V1/bookings/{bookingId}/cancel"
    const val BOOKING_ACCEPT = "$API_V1/bookings/{bookingId}/accept"
    const val BOOKING_DRIVER_CANCEL = "$API_V1/bookings/{bookingId}/driver-cancel"
    const val BOOKING_ARRIVED = "$API_V1/bookings/{bookingId}/arrived"
    const val BOOKING_START = "$API_V1/bookings/{bookingId}/start"
    const val BOOKING_COMPLETE = "$API_V1/bookings/{bookingId}/complete"
}