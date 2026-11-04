package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyServerOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, otpCode: String): Result<String> {
        return authRepository.verifyServerOtp(phoneNumber, otpCode)
    }
}
