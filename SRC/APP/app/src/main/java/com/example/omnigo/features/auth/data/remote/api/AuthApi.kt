package com.example.omnigo.features.auth.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.auth.data.remote.dto.request.ChangePasswordRequest
import com.example.omnigo.features.auth.data.remote.dto.request.CustomerRegisterRequest
import com.example.omnigo.features.auth.data.remote.dto.request.DriverRegisterRequest
import com.example.omnigo.features.auth.data.remote.dto.request.FcmTokenRequest
import com.example.omnigo.features.auth.data.remote.dto.request.LoginRequest
import com.example.omnigo.features.auth.data.remote.dto.request.ResetPasswordRequest
import com.example.omnigo.features.auth.data.remote.dto.request.SendServerOtpRequest
import com.example.omnigo.features.auth.data.remote.dto.request.UpdateUserProfileRequest
import com.example.omnigo.features.auth.data.remote.dto.request.VerifyServerOtpRequest
import com.example.omnigo.features.auth.data.remote.dto.request.VerifyServerOtpResponse
import com.example.omnigo.features.auth.data.remote.dto.response.LoginResponse
import com.example.omnigo.features.auth.data.remote.dto.response.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthApi {

    @GET(ApiEndpoints.GET_ME)
    suspend fun getMe(): Response<ApiResponse<UserResponse>>

    @PUT(ApiEndpoints.GET_ME)
    suspend fun updateProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<ApiResponse<UserResponse>>

    @DELETE(ApiEndpoints.GET_ME)
    suspend fun deleteMyAccount(): Response<ApiResponse<String>>

    @POST(ApiEndpoints.USERS_ME_FCM_TOKEN)
    suspend fun updateFcmToken(
        @Body request: FcmTokenRequest
    ): Response<ApiResponse<String>>

    @Multipart
    @PUT("api/v1/users/me/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<UserResponse>>

    @POST(ApiEndpoints.REGISTER_CUSTOMER)
    suspend fun registerCustomer(
        @Body request: CustomerRegisterRequest
    ): Response<ApiResponse<LoginResponse>>

    @POST(ApiEndpoints.REGISTER_DRIVER)
    suspend fun registerDriver(
        @Body request: DriverRegisterRequest
    ): Response<ApiResponse<LoginResponse>>

    @Multipart
    @POST(ApiEndpoints.DRIVER_UPLOAD_DOCUMENT)
    suspend fun uploadDriverDocument(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>

    @POST(ApiEndpoints.LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @POST(ApiEndpoints.REFRESH_TOKEN)
    suspend fun refreshToken(
        @Header("Cookie") refreshTokenCookie: String
    ): Response<ApiResponse<LoginResponse>>

    @POST(ApiEndpoints.LOGOUT)
    suspend fun logout(
        @Header("Cookie") refreshTokenCookie: String? = null
    ): Response<ApiResponse<String>>

    @POST(ApiEndpoints.OTP_SEND)
    suspend fun sendServerOtp(
        @Body request: SendServerOtpRequest
    ): Response<ApiResponse<String>>

    @POST(ApiEndpoints.OTP_VERIFY)
    suspend fun verifyServerOtp(
        @Body request: VerifyServerOtpRequest
    ): Response<ApiResponse<VerifyServerOtpResponse>>

    @POST(ApiEndpoints.RESET_PASSWORD)
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<String>>

    @POST(ApiEndpoints.CHANGE_PASSWORD)
    suspend fun changePassword(
        @Header("X-User-Id") userId: Long? = null,
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<String>>
}
