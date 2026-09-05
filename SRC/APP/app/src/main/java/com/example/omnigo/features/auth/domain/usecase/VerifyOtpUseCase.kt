package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.VerifyOtpResult
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        verificationId: String,
        otpCode: String
    ): VerifyOtpResult {
        if (verificationId.isBlank()) {
            return VerifyOtpResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)
        }

        if (otpCode.isBlank() || otpCode.length < 6) {
            return VerifyOtpResult.Error(RegisterError.INVALID_OTP)
        }

        return authRepository.verifyOtp(verificationId, otpCode)
    }
}
