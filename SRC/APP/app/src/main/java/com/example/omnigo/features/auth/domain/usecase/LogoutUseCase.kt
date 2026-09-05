package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.model.LogoutResult
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): LogoutResult {
        return authRepository.logout()
    }
}
