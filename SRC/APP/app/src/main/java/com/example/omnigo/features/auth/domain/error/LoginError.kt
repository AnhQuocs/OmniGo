package com.example.omnigo.features.auth.domain.error

enum class LoginError {
    EMPTY_PHONE,
    INVALID_PHONE,
    EMPTY_PASSWORD,
    INVALID_CREDENTIALS,
    ACCOUNT_NOT_FOUND,
    ACCOUNT_BLOCKED,
    SERVER_ERROR,
    NETWORK_ERROR,
    UNKNOWN
}
