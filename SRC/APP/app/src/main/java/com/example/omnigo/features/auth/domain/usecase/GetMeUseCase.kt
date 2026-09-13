package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase lấy thông tin chi tiết của người dùng hiện tại đang đăng nhập.
 */
class GetMeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<AuthUser> {
        return authRepository.getMe()
    }
}
