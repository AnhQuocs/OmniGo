package com.example.omnigo.features.auth.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.auth.data.remote.dto.request.CustomerRegisterRequest
import com.example.omnigo.features.auth.data.remote.dto.request.DriverRegisterRequest
import com.example.omnigo.features.auth.data.remote.dto.request.LoginRequest
import com.example.omnigo.features.auth.data.remote.dto.response.LoginResponse
import com.example.omnigo.features.auth.data.remote.dto.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @GET(ApiEndpoints.GET_ME)
    suspend fun getMe(): Response<ApiResponse<UserResponse>>

    @POST(ApiEndpoints.REGISTER_CUSTOMER)
    suspend fun registerCustomer(
        @Body request: CustomerRegisterRequest
    ): Response<ApiResponse<UserResponse>>

    @POST(ApiEndpoints.REGISTER_DRIVER)
    suspend fun registerDriver(
        @Body request: DriverRegisterRequest
    ): Response<ApiResponse<UserResponse>>

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
}