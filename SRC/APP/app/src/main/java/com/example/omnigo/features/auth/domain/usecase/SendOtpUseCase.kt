package com.example.omnigo.features.auth.domain.usecase

import android.app.Activity
import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.SendOtpResult
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import com.example.omnigo.utils.PhoneUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        phoneNumber: String,
        activity: Activity
    ): Flow<SendOtpResult> {
        if (phoneNumber.isBlank()) {
            return flowOf(SendOtpResult.Error(RegisterError.EMPTY_PHONE))
        }

        if (!PhoneUtils.isValidVietnamesePhoneNumber(phoneNumber)) {
            return flowOf(SendOtpResult.Error(RegisterError.INVALID_PHONE))
        }

        return authRepository.sendOtp(phoneNumber, activity)
    }
}
