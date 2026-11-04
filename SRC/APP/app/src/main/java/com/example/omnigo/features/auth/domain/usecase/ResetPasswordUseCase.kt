package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        phoneNumber: String?,
        email: String?,
        verificationToken: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự"))
        }
        if (newPassword != confirmPassword) {
            return Result.failure(IllegalArgumentException("Mật khẩu xác nhận không khớp"))
        }
        return authRepository.resetPassword(
            phoneNumber = phoneNumber,
            email = email,
            verificationToken = verificationToken,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )
    }
}

