package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.data.remote.dto.request.LoginRequest
import com.example.omnigo.features.auth.domain.error.LoginError
import com.example.omnigo.features.auth.domain.model.LoginResult
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import com.example.omnigo.utils.PhoneUtils
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        phoneNumber: String,
        password: String
    ): LoginResult {
        if (phoneNumber.isBlank()) {
            return LoginResult.Error(LoginError.EMPTY_PHONE)
        }

        if (!PhoneUtils.isValidVietnamesePhoneNumber(phoneNumber)) {
            return LoginResult.Error(LoginError.INVALID_PHONE)
        }

        if (password.isBlank()) {
            return LoginResult.Error(LoginError.EMPTY_PASSWORD)
        }

        val request = LoginRequest(
            phoneNumber = PhoneUtils.toLocal(phoneNumber),
            password = password
        )

        return authRepository.login(request)
    }
}
