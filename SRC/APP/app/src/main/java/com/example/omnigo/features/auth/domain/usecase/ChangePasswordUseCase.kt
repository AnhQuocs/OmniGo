package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        if (oldPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mật khẩu cũ"))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự"))
        }
        if (newPassword == oldPassword) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ"))
        }
        if (newPassword != confirmPassword) {
            return Result.failure(IllegalArgumentException("Mật khẩu xác nhận không khớp"))
        }
        return authRepository.changePassword(
            oldPassword = oldPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )
    }
}

