package com.example.omnigo.core.network

object ApiEndpoints {
    private const val API_V1 = "api/v1"

    // --- AUTHENTICATION ---
    const val LOGIN = "$API_V1/auth/login"
    const val REFRESH_TOKEN = "$API_V1/auth/refresh"
    const val LOGOUT = "$API_V1/auth/logout"
    const val RESET_PASSWORD = "$API_V1/auth/reset-password"
    const val CHANGE_PASSWORD = "$API_V1/auth/change-password"
    const val OTP_SEND = "$API_V1/auth/otp/send"
    const val OTP_VERIFY = "$API_V1/auth/otp/verify"
    const val REGISTER_CUSTOMER = "$API_V1/users/register/customer"
    const val REGISTER_DRIVER = "$API_V1/drivers/register"
    const val DRIVER_UPLOAD_DOCUMENT = "$API_V1/drivers/upload-document"

    const val GET_ME = "$API_V1/users/me"
    const val USERS_ME_FCM_TOKEN = "$API_V1/users/me/fcm-token"

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
    const val DRIVER_PROFILE = "$API_V1/drivers/{id}"

    // --- FOOD REVIEWS ---
    const val FOOD_REVIEW_UPLOAD_IMAGE = "$API_V1/food-reviews/upload-image"
    const val FOOD_REVIEWS = "$API_V1/food-reviews"
    const val FOOD_REVIEW_UPDATE = "$API_V1/food-reviews/{id}"
    const val FOOD_REVIEW_BY_ORDER = "$API_V1/food-reviews/order/{orderId}"
    const val FOOD_REVIEWS_DRIVER = "$API_V1/food-reviews/driver/{driverId}"
    const val RESTAURANT_REVIEWS = "$API_V1/restaurants/{restaurantId}/reviews"

    // --- DRIVER & LOCATION ---
    const val DRIVER_STATUS = "$API_V1/drivers/{driverId}/status"
    const val DRIVER_STATUS_ME = "$API_V1/drivers/me/status"
    const val DRIVER_PROFILE_BY_ID = "$API_V1/drivers/{driverId}/profile"
    const val DRIVER_PROFILE_ME = "$API_V1/drivers/me/profile"
    const val DRIVERS_ME_FCM_TOKEN = "$API_V1/drivers/me/fcm-token"
    const val DRIVER_VEHICLE_UPDATE = "$API_V1/drivers/{driverId}/vehicle"
    const val DRIVER_RESUBMIT = "$API_V1/drivers/{driverId}/resubmit"
    const val DRIVER_LOCATION_ME = "$API_V1/locations/drivers/me"
    const val LOCATIONS_NEARBY = "$API_V1/locations/nearby"

    // --- BOOKING FOOD ORDERS (DRIVER) ---
    const val BOOKING_FOOD_ORDER_ACCEPT = "$API_V1/bookings/food-orders/{orderId}/accept"
    const val BOOKING_FOOD_ORDER_REJECT = "$API_V1/bookings/food-orders/{orderId}/reject"
    const val FOOD_ORDERS_DRIVER = "$API_V1/food-orders/driver"
    const val FOOD_ORDER_DRIVER_STATUS = "$API_V1/food-orders/{orderId}/driver-status"

    // --- RIDE & PRICING & VOUCHERS ---
    const val PRICING_CALCULATE = "$API_V1/pricing/calculate"
    const val PRICING_DELIVERY_FEE = "$API_V1/pricing/delivery-fee"
    const val PRICING_ROUTE = "$API_V1/pricing/route"
    const val VOUCHERS = "$API_V1/pricing/vouchers"
    const val VOUCHERS_BROWSE = "$API_V1/pricing/vouchers/browse"
    const val VOUCHERS_APPLICABLE = "$API_V1/pricing/vouchers/applicable"
    const val VOUCHERS_APPLY = "$API_V1/pricing/vouchers/apply"
    const val VOUCHERS_COMMIT = "$API_V1/pricing/vouchers/commit"
    const val VOUCHERS_ROLLBACK = "$API_V1/pricing/vouchers/rollback"
    const val VOUCHERS_MY_USAGES = "$API_V1/pricing/vouchers/my-usages"
    const val SAVED_ADDRESSES = "$API_V1/users/me/addresses"
    const val SAVED_ADDRESS_BY_ID = "$API_V1/users/me/addresses/{id}"
    const val SAVED_ADDRESS_DEFAULT = "$API_V1/users/me/addresses/{addressId}/default"
    const val BOOKINGS = "$API_V1/bookings"
    const val BOOKING_DETAIL = "$API_V1/bookings/{bookingId}"
    const val BOOKING_REVIEWS = "$API_V1/bookings/{bookingId}/reviews"
    const val BOOKING_DRIVER_REVIEWS = "$API_V1/bookings/driver/{driverId}/reviews"
    const val BOOKING_DRIVER = "$API_V1/bookings/{bookingId}/driver"
    const val BOOKING_CANCEL = "$API_V1/bookings/{bookingId}/cancel"
    const val BOOKING_ACCEPT = "$API_V1/bookings/{bookingId}/accept"
    const val BOOKING_DRIVER_CANCEL = "$API_V1/bookings/{bookingId}/driver-cancel"
    const val BOOKING_ARRIVED = "$API_V1/bookings/{bookingId}/arrived"
    const val BOOKING_START = "$API_V1/bookings/{bookingId}/start"
    const val BOOKING_COMPLETE = "$API_V1/bookings/{bookingId}/complete"
    const val BOOKINGS_CUSTOMER = "$API_V1/bookings/customer"
    const val BOOKINGS_DRIVER = "$API_V1/bookings/driver"
    const val BOOKINGS_DRIVER_DAILY_REPORT = "$API_V1/bookings/driver/daily-report"

    // --- PAYMENTS & WALLET ---
    const val PAYMENTS_WALLET = "$API_V1/payments/wallet"
    const val WALLET_DEPOSIT = "$API_V1/payments/wallet/deposit"
    const val WALLET_WITHDRAW = "$API_V1/payments/wallet/withdraw"
    const val PAYMENTS_TRANSACTIONS = "$API_V1/payments/transactions"
    const val DRIVER_WITHDRAW = "$API_V1/payments/driver/withdraw"
    const val PAYMENT_CREATE = "$API_V1/payments/payment/create"
    const val BOOKING_PAYMENT_STATUS = "$API_V1/payments/booking/{bookingId}/payment-status"
    const val FOOD_ORDER_PAYMENT_STATUS = "$API_V1/payments/food-order/{orderId}/payment-status"
    const val PLATFORM_FEE = "$API_V1/payments/platform-fee"
}
