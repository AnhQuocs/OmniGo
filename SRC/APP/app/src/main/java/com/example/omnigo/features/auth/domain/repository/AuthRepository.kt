package com.example.omnigo.features.auth.domain.repository

import android.app.Activity
import com.example.omnigo.features.auth.data.remote.dto.request.LoginRequest
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.LoginResult
import com.example.omnigo.features.auth.domain.model.LogoutResult
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.SendOtpResult
import com.example.omnigo.features.auth.domain.model.VerifyOtpResult
import com.example.omnigo.features.auth.domain.usecase.RegisterResult
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface AuthRepository {

    suspend fun sendOtp(
        phoneNumber: String,
        activity: Activity
    ): Flow<SendOtpResult>

    suspend fun verifyOtp(
        verificationId: String,
        otpCode: String
    ): VerifyOtpResult

    suspend fun sendServerOtp(
        phoneNumber: String
    ): Result<String> = Result.success("")

    suspend fun verifyServerOtp(
        phoneNumber: String,
        otpCode: String
    ): Result<String> = Result.success("DEFAULT_TOKEN")

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

    suspend fun getMe(): Flow<AuthUser>

    suspend fun uploadDriverDocument(
        file: MultipartBody.Part
    ): Result<String>

    suspend fun uploadAvatar(
        file: MultipartBody.Part
    ): Result<String>

    suspend fun resetPassword(
        phoneNumber: String?,
        email: String?,
        verificationToken: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String>

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String>

    suspend fun updateProfile(
        fullName: String,
        email: String?
    ): Result<AuthUser>

    suspend fun deleteMyAccount(): Result<String> = Result.success("")

    suspend fun updateFcmToken(fcmToken: String): Result<String> = Result.success("")
}
