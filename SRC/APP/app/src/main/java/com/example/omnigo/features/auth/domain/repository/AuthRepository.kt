package com.example.omnigo.features.auth.domain.repository

import android.app.Activity
import com.example.omnigo.features.auth.data.remote.dto.request.LoginRequest
import com.example.omnigo.features.auth.domain.model.LoginResult
import com.example.omnigo.features.auth.domain.model.LogoutResult
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.SendOtpResult
import com.example.omnigo.features.auth.domain.model.VerifyOtpResult
import com.example.omnigo.features.auth.domain.usecase.RegisterResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun sendOtp(
        phoneNumber: String,
        activity: Activity
    ): Flow<SendOtpResult>

    suspend fun verifyOtp(
        verificationId: String,
        otpCode: String
    ): VerifyOtpResult

    suspend fun registerCustomer(
        user: RegisterUser
    ): RegisterResult

    suspend fun registerDriver(
        driver: RegisterDriver
    ): RegisterResult

    suspend fun login(
        request: LoginRequest
    ): LoginResult

    suspend fun logout(): LogoutResult

    suspend fun refreshToken(): Boolean
}