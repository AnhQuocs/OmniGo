package com.example.omnigo.features.auth.data.repository

import android.app.Activity
import android.util.Log
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.network.dto.ApiResponse
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

private const val TAG = "AuthRepositoryImpl"

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
        Log.d(TAG, "sendOtp: Starting OTP request for phone=$phoneNumber (E.164: $e164Phone)")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "sendOtp - onVerificationCompleted: Instant verification / auto-retrieval completed.")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val authResult = firebaseAuth.signInWithCredential(credential).await()
                        val token = authResult.user?.getIdToken(false)?.await()?.token
                        if (!token.isNullOrBlank()) {
                            Log.d(TAG, "sendOtp - onVerificationCompleted: Token obtained successfully.")
                            trySend(SendOtpResult.AutoVerified(token))
                        } else {
                            Log.w(TAG, "sendOtp - onVerificationCompleted: Token is null or blank.")
                            trySend(SendOtpResult.Error(RegisterError.UNKNOWN))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "sendOtp - onVerificationCompleted: Auto-verification sign-in failed", e)
                        trySend(SendOtpResult.Error(RegisterError.UNKNOWN))
                    } finally {
                        close()
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.e(TAG, "sendOtp - onVerificationFailed: ${exception::class.java.simpleName} - ${exception.message}", exception)
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
                Log.d(TAG, "sendOtp - onCodeSent: OTP code sent successfully. verificationId=$verificationId")
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
        Log.d(TAG, "verifyOtp: Verifying OTP for verificationId=$verificationId")
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
            if (!firebaseToken.isNullOrBlank()) {
                Log.d(TAG, "verifyOtp: OTP verified successfully. Firebase token acquired.")
                VerifyOtpResult.Success(firebaseToken)
            } else {
                Log.w(TAG, "verifyOtp: OTP verified but Firebase token is null or blank.")
                VerifyOtpResult.Error(RegisterError.UNKNOWN)
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "verifyOtp: Invalid OTP code entered", e)
            VerifyOtpResult.Error(RegisterError.INVALID_OTP)
        } catch (e: FirebaseTooManyRequestsException) {
            Log.e(TAG, "verifyOtp: Too many requests during OTP verification", e)
            VerifyOtpResult.Error(RegisterError.TOO_MANY_REQUESTS)
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtp: Unexpected error during OTP verification", e)
            VerifyOtpResult.Error(RegisterError.UNKNOWN)
        }
    }

    override suspend fun registerCustomer(
        user: RegisterUser
    ): RegisterResult {
        Log.d(TAG, "registerCustomer: Checking Firebase phone verification session for phone=${user.phoneNumber}")
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "registerCustomer: No authenticated Firebase user found. Phone verification required.")
            return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)
        }

        // Đảm bảo số điện thoại đăng ký khớp với số điện thoại đã xác thực OTP trên Firebase
        if (!PhoneUtils.isSamePhoneNumber(currentUser.phoneNumber, user.phoneNumber)) {
            Log.w(TAG, "registerCustomer: Phone mismatch. Firebase phone=${currentUser.phoneNumber}, Input phone=${user.phoneNumber}")
            return RegisterResult.Error(RegisterError.PHONE_NUMBER_MISMATCH)
        }

        val firebaseToken = getFirebaseToken()
        if (firebaseToken == null) {
            Log.w(TAG, "registerCustomer: Failed to obtain Firebase ID token.")
            return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)
        }

        val request = user.toCustomerRequest(firebaseToken = firebaseToken)

        return try {
            val response = authApi.registerCustomer(request)
            handleApiResponse(response)
        } catch (e: IOException) {
            Log.e(TAG, "registerCustomer: Network error during customer registration", e)
            RegisterResult.Error(RegisterError.NETWORK_ERROR)
        } catch (e: Exception) {
            Log.e(TAG, "registerCustomer: Unexpected error during customer registration", e)
            RegisterResult.Error(RegisterError.UNKNOWN)
        }
    }

    override suspend fun registerDriver(
        driver: RegisterDriver
    ): RegisterResult {
        Log.d(TAG, "registerDriver: Checking Firebase phone verification session for phone=${driver.user.phoneNumber}")
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "registerDriver: No authenticated Firebase user found. Phone verification required.")
            return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)
        }

        // Đảm bảo số điện thoại đăng ký khớp với số điện thoại đã xác thực OTP trên Firebase
        if (!PhoneUtils.isSamePhoneNumber(currentUser.phoneNumber, driver.user.phoneNumber)) {
            Log.w(TAG, "registerDriver: Phone mismatch. Firebase phone=${currentUser.phoneNumber}, Input phone=${driver.user.phoneNumber}")
            return RegisterResult.Error(RegisterError.PHONE_NUMBER_MISMATCH)
        }

        val firebaseToken = getFirebaseToken()
        if (firebaseToken == null) {
            Log.w(TAG, "registerDriver: Failed to obtain Firebase ID token.")
            return RegisterResult.Error(RegisterError.PHONE_VERIFICATION_REQUIRED)
        }

        val request = driver.toDriverRequest(firebaseToken = firebaseToken)

        return try {
            val response = authApi.registerDriver(request)
            handleApiResponse(response)
        } catch (e: IOException) {
            Log.e(TAG, "registerDriver: Network error during driver registration", e)
            RegisterResult.Error(RegisterError.NETWORK_ERROR)
        } catch (e: Exception) {
            Log.e(TAG, "registerDriver: Unexpected error during driver registration", e)
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
                        phoneNumber = loginData.user.phoneNumber.orEmpty(),
                        fullName = loginData.user.fullName.orEmpty(),
                        role = loginData.user.role.orEmpty()
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
                        phoneNumber = loginData.user.phoneNumber.orEmpty(),
                        fullName = loginData.user.fullName.orEmpty(),
                        role = loginData.user.role.orEmpty()
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
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "getFirebaseToken: No current Firebase user found")
            return null
        }
        return try {
            val tokenResult = currentUser.getIdToken(false).await()
            tokenResult.token
        } catch (e: Exception) {
            Log.e(TAG, "getFirebaseToken: Error acquiring Firebase ID token", e)
            null
        }
    }

    private suspend fun handleApiResponse(response: Response<ApiResponse<UserResponse>>): RegisterResult {
        return if (response.isSuccessful) {
            val apiResponse = response.body()
            val userResponse = apiResponse?.data
            if (apiResponse?.success == true && userResponse != null) {
                val accessToken = userResponse.accessToken
                val refreshToken = userResponse.refreshToken
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
                Log.d(TAG, "handleApiResponse: Registration successful for user ID=${userResponse.id}")
                RegisterResult.Success(userResponse.toDomain())
            } else {
                Log.w(TAG, "handleApiResponse: API response success=false or data is null: ${apiResponse?.message}")
                RegisterResult.Error(RegisterError.UNKNOWN)
            }
        } else {
            Log.e(TAG, "handleApiResponse: Registration failed with HTTP code=${response.code()}")
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