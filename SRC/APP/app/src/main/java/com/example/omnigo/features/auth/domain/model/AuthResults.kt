package com.example.omnigo.features.auth.domain.model

import com.example.omnigo.features.auth.domain.error.LoginError
import com.example.omnigo.utils.UiText

sealed interface LoginResult {
    data class Success(
        val user: AuthUser
    ) : LoginResult

    data class Error(
        val error: LoginError
    ) : LoginResult
}

sealed interface LogoutResult {
    data object Success : LogoutResult

    data class Error(
        val message: UiText
    ) : LogoutResult
}
