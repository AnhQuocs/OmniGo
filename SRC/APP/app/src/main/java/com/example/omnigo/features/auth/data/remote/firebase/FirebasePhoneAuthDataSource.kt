package com.example.omnigo.features.auth.data.remote.firebase

import android.app.Activity
import android.util.Log
import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.SendOtpResult
import com.example.omnigo.features.auth.domain.model.VerifyOtpResult
import com.example.omnigo.utils.PhoneUtils
import com.example.omnigo.utils.await
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirebaseAuthDataSource"

@Singleton
class FirebasePhoneAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun sendOtp(
        phoneNumber: String,
        activity: Activity
    ): Flow<SendOtpResult> = callbackFlow {
        val e164Phone = PhoneUtils.toE164(phoneNumber)
        Log.d(TAG, "sendOtp: Starting OTP request for phone=$phoneNumber (E.164: $e164Phone)")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "sendOtp - onVerificationCompleted: Instant verification completed.")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val authResult = firebaseAuth.signInWithCredential(credential).await()
                        val token = authResult.user?.getIdToken(false)?.await()?.token
                        if (!token.isNullOrBlank()) {
                            Log.d(TAG, "sendOtp - onVerificationCompleted: Token obtained.")
                            trySend(SendOtpResult.AutoVerified(token))
                        } else {
                            trySend(SendOtpResult.Error(RegisterError.UNKNOWN))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "sendOtp - onVerificationCompleted: Sign-in failed", e)
                        trySend(SendOtpResult.Error(RegisterError.UNKNOWN))
                    } finally {
                        close()
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.e(TAG, "sendOtp - onVerificationFailed: ${exception::class.java.simpleName}", exception)
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
                Log.d(TAG, "sendOtp - onCodeSent: verificationId=$verificationId")
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

    suspend fun verifyOtp(
        verificationId: String,
        otpCode: String
    ): VerifyOtpResult {
        Log.d(TAG, "verifyOtp: Verifying OTP for verificationId=$verificationId")
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
            if (!firebaseToken.isNullOrBlank()) {
                Log.d(TAG, "verifyOtp: OTP verified successfully.")
                VerifyOtpResult.Success(firebaseToken)
            } else {
                VerifyOtpResult.Error(RegisterError.UNKNOWN)
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "verifyOtp: Invalid OTP code entered", e)
            VerifyOtpResult.Error(RegisterError.INVALID_OTP)
        } catch (e: FirebaseTooManyRequestsException) {
            Log.e(TAG, "verifyOtp: Too many requests", e)
            VerifyOtpResult.Error(RegisterError.TOO_MANY_REQUESTS)
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtp: Unexpected error", e)
            VerifyOtpResult.Error(RegisterError.UNKNOWN)
        }
    }

    suspend fun validatePhoneSession(phone: String): Pair<String?, RegisterError?> {
        val currentUser = firebaseAuth.currentUser
            ?: return Pair(null, RegisterError.PHONE_VERIFICATION_REQUIRED)
        if (!PhoneUtils.isSamePhoneNumber(currentUser.phoneNumber, phone)) {
            return Pair(null, RegisterError.PHONE_NUMBER_MISMATCH)
        }
        val token = getFirebaseToken()
            ?: return Pair(null, RegisterError.PHONE_VERIFICATION_REQUIRED)
        return Pair(token, null)
    }

    suspend fun getFirebaseToken(): String? {
        val user = firebaseAuth.currentUser ?: return null
        return try {
            user.getIdToken(false).await()?.token
        } catch (e: Exception) {
            Log.e(TAG, "getFirebaseToken: Error acquiring ID token", e)
            null
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

