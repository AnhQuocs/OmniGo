package com.example.omnigo.features.auth.data.repository

import android.app.Activity
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.data.mapper.toCustomerRequest
import com.example.omnigo.features.auth.data.mapper.toDomain
import com.example.omnigo.features.auth.data.mapper.toDriverRequest
import com.example.omnigo.features.auth.data.remote.api.AuthApi
import com.example.omnigo.features.auth.data.remote.dto.response.UserResponse
import com.example.omnigo.features.auth.data.remote.dto.request.LoginRequest
import com.example.omnigo.features.auth.domain.error.LoginError
import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.LoginResult
import com.example.omnigo.features.auth.domain.model.LogoutResult
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.SendOtpResult
import com.example.omnigo.features.auth.domain.model.VerifyOtpResult
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import com.example.omnigo.features.auth.domain.usecase.RegisterResult
import com.example.omnigo.utils.PhoneUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val firebaseAuth: FirebaseAuth,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun sendOtp(
        phoneNumber: String,
        activity: Activity
    ): Flow<SendOtpResult> = callbackFlow {
        val e164Phone = PhoneUtils.toE164(phoneNumber)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val authResult = firebaseAuth.signInWithCredential(credential).await()
                        val token = authResult.user?.getIdToken(false)?.await()?.token
                        if (!token.isNullOrBlank()) {
                            trySend(SendOtpResult.AutoVerified(token))
                        } else {
                            trySend(SendOtpResult.Error(RegisterError.UNKNOWN))
                        }
                    } catch (e: Exception) {
                        trySend(SendOtpResult.Error(RegisterError.UNKNOWN))
                    } finally {
                        close()
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                val error = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> RegisterError.INVALID_PHONE
                    is FirebaseTooManyRequestsException -> RegisterError.TOO_MANY_REQUESTS
                    else -> RegisterError.UNKNOWN
                }
                trySend(SendOtpResult.Error(error))
                close()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(SendOtpResult.CodeSent(verificationId))
                close()
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(e164Phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose { }
    }

    override suspend fun verifyOtp(
        verificationId: String,
        otpCode: String
    ): VerifyOtpResult {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
            if (!firebaseToken.isNullOrBlank()) {
                VerifyOtpResult.Success(firebaseToken)
            } else {
                VerifyOtpResult.Error(RegisterError.UNKNOWN)
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            VerifyOtpResult.Error(RegisterError.INVALID_OTP)
        } catch (e: FirebaseTooManyRequestsException) {
            VerifyOtpResult.Error(RegisterError.TOO_MANY_REQUESTS)
        } catch (e: Exception) {
            VerifyOtpResult.Error(RegisterError.UNKNOWN)
        }
    }

    override suspend fun registerCustomer(
        user: RegisterUser
    ): RegisterResult {
        val currentUser = firebaseAuth.currentUser
            ?: return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)

        // Đảm bảo số điện thoại đăng ký khớp với số điện thoại đã xác thực OTP trên Firebase
        if (!PhoneUtils.isSamePhoneNumber(currentUser.phoneNumber, user.phoneNumber)) {
            return RegisterResult.Error(RegisterError.PHONE_NUMBER_MISMATCH)
        }

        val firebaseToken = getFirebaseToken()
            ?: return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)

        val request = user.toCustomerRequest(firebaseToken = firebaseToken)

        return try {
            val response = authApi.registerCustomer(request)
            handleApiResponse(response)
        } catch (e: IOException) {
            RegisterResult.Error(RegisterError.NETWORK_ERROR)
        } catch (e: Exception) {
            RegisterResult.Error(RegisterError.UNKNOWN)
        }
    }

    override suspend fun registerDriver(
        driver: RegisterDriver
    ): RegisterResult {
        val currentUser = firebaseAuth.currentUser
            ?: return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)

        // Đảm bảo số điện thoại đăng ký khớp với số điện thoại đã xác thực OTP trên Firebase
        if (!PhoneUtils.isSamePhoneNumber(currentUser.phoneNumber, driver.user.phoneNumber)) {
            return RegisterResult.Error(RegisterError.PHONE_NUMBER_MISMATCH)
        }

        val firebaseToken = getFirebaseToken()
            ?: return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)

        val request = driver.toDriverRequest(firebaseToken = firebaseToken)

        return try {
            val response = authApi.registerDriver(request)
            handleApiResponse(response)
        } catch (e: IOException) {
            RegisterResult.Error(RegisterError.NETWORK_ERROR)
        } catch (e: Exception) {
            RegisterResult.Error(RegisterError.UNKNOWN)
        }
    }

    override suspend fun login(
        request: LoginRequest
    ): LoginResult {
        return try {
            val response = authApi.login(request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                val loginData = apiResponse?.data
                if (apiResponse?.success == true && loginData != null) {
                    sessionManager.saveSession(
                        accessToken = loginData.accessToken,
                        refreshToken = loginData.refreshToken,
                        userId = loginData.user.id,
                        phoneNumber = loginData.user.phoneNumber,
                        fullName = loginData.user.fullName,
                        role = loginData.user.role
                    )
                    LoginResult.Success(loginData.user.toDomain())
                } else {
                    LoginResult.Error(LoginError.UNKNOWN)
                }
            } else {
                val error = when (response.code()) {
                    400, 401 -> LoginError.INVALID_CREDENTIALS
                    403 -> LoginError.ACCOUNT_BLOCKED
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
        try {
            val token = sessionManager.getAccessToken()
            val refreshToken = sessionManager.getRefreshToken()
            val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null
            val cookieHeader = if (!refreshToken.isNullOrBlank()) "refresh_token=$refreshToken" else null

            authApi.logout(accessToken = authHeader, refreshTokenCookie = cookieHeader)
        } catch (_: Exception) {
            // Ngay cả khi API logout lỗi mạng, vẫn xóa session local để đảm bảo bảo mật cho người dùng
        } finally {
            sessionManager.clearSession()
            firebaseAuth.signOut()
        }
        return LogoutResult.Success
    }

    override suspend fun refreshToken(): Boolean {
        val oldToken = sessionManager.getAccessToken() ?: return false
        val refreshToken = sessionManager.getRefreshToken() ?: return false

        return try {
            val response = authApi.refreshToken(
                oldAccessToken = "Bearer $oldToken",
                refreshTokenCookie = "refresh_token=$refreshToken"
            )
            if (response.isSuccessful) {
                val loginData = response.body()?.data
                if (loginData != null) {
                    sessionManager.saveSession(
                        accessToken = loginData.accessToken,
                        refreshToken = loginData.refreshToken,
                        userId = loginData.user.id,
                        phoneNumber = loginData.user.phoneNumber,
                        fullName = loginData.user.fullName,
                        role = loginData.user.role
                    )
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getFirebaseToken(): String? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return try {
            val tokenResult = currentUser.getIdToken(false).await()
            tokenResult.token
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun handleApiResponse(response: Response<UserResponse>): RegisterResult {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                // Lưu token và thông tin phiên đăng nhập vào DataStore
                sessionManager.saveSession(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    userId = body.id,
                    phoneNumber = body.phoneNumber,
                    fullName = body.fullName,
                    role = body.role
                )
                RegisterResult.Success(body.toDomain())
            } else {
                RegisterResult.Error(RegisterError.UNKNOWN)
            }
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

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}