package com.example.omnigo.features.auth.domain.model

import com.example.omnigo.features.auth.domain.error.RegisterError

sealed interface SendOtpResult {
    data class CodeSent(
        val verificationId: String
    ) : SendOtpResult

    data class AutoVerified(
        val firebaseToken: String
    ) : SendOtpResult

    data class Error(
        val error: RegisterError
    ) : SendOtpResult
}

sealed interface VerifyOtpResult {
    data class Success(
        val firebaseToken: String
    ) : VerifyOtpResult

    data class Error(
        val error: RegisterError
    ) : VerifyOtpResult
}
