package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SyncFcmTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(fcmToken: String): Result<String> {
        return authRepository.updateFcmToken(fcmToken)
    }
}
