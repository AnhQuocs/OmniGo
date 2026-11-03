package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(fullName: String, email: String?): Result<AuthUser> {
        val trimmedName = fullName.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Họ và tên không được để trống"))
        }

        val trimmedEmail = email?.trim()?.ifBlank { null }
        if (trimmedEmail != null) {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
            if (!trimmedEmail.matches(emailRegex)) {
                return Result.failure(IllegalArgumentException("Địa chỉ email không đúng định dạng"))
            }
        }

        return authRepository.updateProfile(trimmedName, trimmedEmail)
    }
}

