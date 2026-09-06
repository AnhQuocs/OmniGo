package com.example.omnigo.features.auth.domain.error

enum class RegisterError {
    EMPTY_FULL_NAME,
    EMPTY_EMAIL,
    INVALID_EMAIL,
    EMPTY_PHONE,
    INVALID_PHONE,
    EMPTY_PASSWORD,
    PASSWORD_TOO_SHORT,
    PHONE_VERIFICATION_REQUIRED,

    EMPTY_LICENSE_PLATE,
    EMPTY_VEHICLE_MODEL,

    // OTP / Phone Auth
    INVALID_OTP,
    OTP_EXPIRED,
    TOO_MANY_REQUESTS,
    PHONE_NUMBER_MISMATCH,

    // Network / Server
    EMAIL_OR_PHONE_ALREADY_EXISTS,
    BAD_REQUEST,
    SERVER_ERROR,
    NETWORK_ERROR,
    UNKNOWN
}