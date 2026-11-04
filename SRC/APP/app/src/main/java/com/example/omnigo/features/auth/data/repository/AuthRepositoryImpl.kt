package com.example.omnigo.features.auth.data.repository

import android.app.Activity
import android.util.Log
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.core.network.toBackendMessage
import com.example.omnigo.features.auth.data.mapper.toCustomerRequest
import com.example.omnigo.features.auth.data.mapper.toDomain
import com.example.omnigo.features.auth.data.mapper.toDriverRequest
import com.example.omnigo.features.auth.data.remote.api.AuthApi
import com.example.omnigo.features.auth.data.remote.dto.request.ChangePasswordRequest
import com.example.omnigo.features.auth.data.remote.dto.request.FcmTokenRequest
import com.example.omnigo.features.auth.data.remote.dto.request.LoginRequest
import com.example.omnigo.features.auth.data.remote.dto.request.ResetPasswordRequest
import com.example.omnigo.features.auth.data.remote.dto.request.SendServerOtpRequest
import com.example.omnigo.features.auth.data.remote.dto.request.UpdateUserProfileRequest
import com.example.omnigo.features.auth.data.remote.dto.request.VerifyServerOtpRequest
import com.example.omnigo.features.auth.data.remote.dto.response.LoginResponse
import com.example.omnigo.features.auth.data.remote.firebase.FirebasePhoneAuthDataSource
import com.example.omnigo.features.auth.domain.error.LoginError
import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.LoginResult
import com.example.omnigo.features.auth.domain.model.LogoutResult
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.SendOtpResult
import com.example.omnigo.features.auth.domain.model.VerifyOtpResult
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import com.example.omnigo.features.auth.domain.usecase.RegisterResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

private const val TAG = "AuthRepositoryImpl"

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val firebasePhoneAuthDataSource: FirebasePhoneAuthDataSource,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun sendOtp(phoneNumber: String, activity: Activity): Flow<SendOtpResult> =
        firebasePhoneAuthDataSource.sendOtp(phoneNumber, activity)

    override suspend fun verifyOtp(verificationId: String, otpCode: String): VerifyOtpResult =
        firebasePhoneAuthDataSource.verifyOtp(verificationId, otpCode)

    override suspend fun sendServerOtp(phoneNumber: String): Result<String> {
        return try {
            val response = authApi.sendServerOtp(SendServerOtpRequest(phoneNumber = phoneNumber))
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data ?: body.message ?: "OTP sent")
            } else {
                Result.failure(Exception(body?.message ?: "Failed to send OTP (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyServerOtp(phoneNumber: String, otpCode: String): Result<String> {
        return try {
            val response = authApi.verifyServerOtp(
                VerifyServerOtpRequest(phoneNumber = phoneNumber, otp = otpCode)
            )
            val body = response.body()
            val token = body?.data?.verificationToken
            if (response.isSuccessful && body?.success == true && !token.isNullOrBlank()) {
                Result.success(token)
            } else {
                Result.failure(Exception(body?.message ?: "Invalid OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerCustomer(user: RegisterUser): RegisterResult {
        return try {
            handleApiResponse(authApi.registerCustomer(user.toCustomerRequest()))
        } catch (e: Exception) {
            RegisterResult.Error(RegisterError.NETWORK_ERROR)
        }
    }

    override suspend fun registerDriver(driver: RegisterDriver): RegisterResult {
        return try {
            handleApiResponse(authApi.registerDriver(driver.toDriverRequest()))
        } catch (e: Exception) {
            RegisterResult.Error(RegisterError.NETWORK_ERROR)
        }
    }

    override suspend fun login(request: LoginRequest): LoginResult {
        return try {
            val response = authApi.login(request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                val loginResponse = apiResponse?.data
                if (apiResponse?.success == true && loginResponse != null) {
                    val accessToken = loginResponse.accessToken
                    val refreshToken = loginResponse.refreshToken
                    val userResponse = loginResponse.user
                    if (!accessToken.isNullOrBlank()) {
                        sessionManager.saveSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            userId = userResponse.id,
                            phoneNumber = userResponse.phoneNumber.orEmpty(),
                            fullName = userResponse.fullName.orEmpty(),
                            role = userResponse.role.orEmpty()
                        )
                    }
                    LoginResult.Success(userResponse.toDomain())
                } else {
                    LoginResult.Error(LoginError.UNKNOWN)
                }
            } else {
                val error = when (response.code()) {
                    400 -> LoginError.INVALID_CREDENTIALS
                    404 -> LoginError.ACCOUNT_NOT_FOUND
                    in 500..599 -> LoginError.SERVER_ERROR
                    else -> LoginError.UNKNOWN
                }
                LoginResult.Error(error)
            }
        } catch (e: IOException) {
            LoginResult.Error(LoginError.NETWORK_ERROR)
        } catch (e: Exception) {
            LoginResult.Error(LoginError.UNKNOWN)
        }
    }

    override suspend fun logout(): LogoutResult {
        return try {
            val refreshToken = sessionManager.getRefreshToken()
            val refreshTokenCookie = if (!refreshToken.isNullOrBlank()) "refreshToken=$refreshToken" else null
            authApi.logout(refreshTokenCookie)
            sessionManager.clearSession()
            LogoutResult.Success
        } catch (e: Exception) {
            sessionManager.clearSession()
            LogoutResult.Success
        }
    }

    override suspend fun refreshToken(): Boolean {
        return try {
            val refreshToken = sessionManager.getRefreshToken() ?: return false
            val response = authApi.refreshToken("refreshToken=$refreshToken")
            if (response.isSuccessful && response.body()?.success == true) {
                val loginResponse = response.body()?.data
                val newAccessToken = loginResponse?.accessToken
                val newRefreshToken = loginResponse?.refreshToken
                if (!newAccessToken.isNullOrBlank()) {
                    sessionManager.saveSession(
                        accessToken = newAccessToken,
                        refreshToken = newRefreshToken,
                        userId = null,
                        phoneNumber = null,
                        fullName = null,
                        role = null
                    )
                    true
                } else false
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getMe(): Flow<AuthUser> = flow {
        try {
            val response = authApi.getMe()
            if (response.isSuccessful && response.body()?.success == true) {
                val userResponse = response.body()?.data
                if (userResponse != null) {
                    val authUser = userResponse.toDomain()
                    val currentAccess = sessionManager.getAccessToken().orEmpty()
                    val currentRefresh = sessionManager.getRefreshToken()
                    sessionManager.saveSession(
                        accessToken = currentAccess,
                        refreshToken = currentRefresh,
                        userId = authUser.id,
                        phoneNumber = authUser.phoneNumber,
                        fullName = authUser.fullName,
                        role = authUser.role.name
                    )
                    emit(authUser)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMe error: ${e.message}", e)
        }
    }

    override suspend fun uploadDriverDocument(file: MultipartBody.Part): Result<String> {
        return try {
            val response = authApi.uploadDriverDocument(file)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                val docUrl = data?.get("documentUrl") ?: data?.get("url")
                if (!docUrl.isNullOrBlank()) Result.success(docUrl)
                else Result.failure(Exception("Document URL missing in response"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(file: MultipartBody.Part): Result<String> {
        return try {
            val response = authApi.uploadAvatar(file)
            if (response.isSuccessful && response.body()?.success == true) {
                val avatarUrl = response.body()?.data?.avatarUrl
                if (!avatarUrl.isNullOrBlank()) {
                    sessionManager.saveAvatarUrl(avatarUrl)
                    Result.success(avatarUrl)
                } else Result.failure(Exception("Avatar URL missing in response"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Upload avatar failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(
        phoneNumber: String?,
        email: String?,
        verificationToken: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        return try {
            val request = ResetPasswordRequest(
                phoneNumber = phoneNumber,
                email = email,
                verificationToken = verificationToken,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
            val response = authApi.resetPassword(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Đặt lại mật khẩu thành công")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Đặt lại mật khẩu thất bại (HTTP ${response.code()})"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(e.toBackendMessage() ?: e.message()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        return try {
            val userId = sessionManager.getUserId()
            val request = ChangePasswordRequest(oldPassword, newPassword, confirmPassword)
            val response = authApi.changePassword(userId = userId, request = request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Đổi mật khẩu thành công")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Đổi mật khẩu thất bại (HTTP ${response.code()})"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(e.toBackendMessage() ?: e.message()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(fullName: String, email: String?): Result<AuthUser> {
        return try {
            val response = authApi.updateProfile(UpdateUserProfileRequest(fullName = fullName, email = email))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val updatedUser = response.body()!!.data!!.toDomain()
                val currentAccess = sessionManager.getAccessToken().orEmpty()
                val currentRefresh = sessionManager.getRefreshToken()
                sessionManager.saveSession(
                    accessToken = currentAccess,
                    refreshToken = currentRefresh,
                    userId = updatedUser.id,
                    phoneNumber = updatedUser.phoneNumber,
                    fullName = updatedUser.fullName,
                    role = updatedUser.role.name
                )
                Result.success(updatedUser)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Cập nhật thông tin thất bại (HTTP ${response.code()})"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(e.toBackendMessage() ?: e.message()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMyAccount(): Result<String> {
        return try {
            val response = authApi.deleteMyAccount()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                sessionManager.clearSession()
                Result.success(body.message ?: "Account deactivated")
            } else {
                Result.failure(Exception(body?.message ?: "Failed to deactivate account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFcmToken(fcmToken: String): Result<String> {
        return try {
            val response = authApi.updateFcmToken(FcmTokenRequest(fcmToken = fcmToken))
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.message ?: "FCM token synced")
            } else {
                Result.failure(Exception(body?.message ?: "Failed to sync FCM token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleApiResponse(response: Response<ApiResponse<LoginResponse>>): RegisterResult {
        return if (response.isSuccessful) {
            val apiResponse = response.body()
            val loginResponse = apiResponse?.data
            if (apiResponse?.success == true && loginResponse != null) {
                val accessToken = loginResponse.accessToken
                val refreshToken = loginResponse.refreshToken
                val userResponse = loginResponse.user
                if (!accessToken.isNullOrBlank()) {
                    sessionManager.saveSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = userResponse.id,
                        phoneNumber = userResponse.phoneNumber.orEmpty(),
                        fullName = userResponse.fullName.orEmpty(),
                        role = userResponse.role.orEmpty()
                    )
                }
                RegisterResult.Success(userResponse.toDomain())
            } else RegisterResult.Error(RegisterError.UNKNOWN)
        } else {
            val error = when (response.code()) {
                400 -> RegisterError.BAD_REQUEST
                409 -> RegisterError.EMAIL_OR_PHONE_ALREADY_EXISTS
                in 500..599 -> RegisterError.SERVER_ERROR
                else -> RegisterError.UNKNOWN
            }
            RegisterResult.Error(error)
        }
    }
}
