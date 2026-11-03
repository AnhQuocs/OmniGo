package com.example.omnigo.features.customer.address.data.remote.api

import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.address.data.remote.dto.SaveAddressRequestDto
import com.example.omnigo.features.customer.address.data.remote.dto.SavedAddressResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserAddressApi {

    @GET("api/v1/users/me/addresses")
    suspend fun getSavedAddresses(
        @Header("X-User-Id") userId: Long? = null
    ): Response<ApiResponse<List<SavedAddressResponseDto>>>

    @POST("api/v1/users/me/addresses")
    suspend fun saveAddress(
        @Header("X-User-Id") userId: Long? = null,
        @Body request: SaveAddressRequestDto
    ): Response<ApiResponse<SavedAddressResponseDto>>

    @PUT("api/v1/users/me/addresses/{addressId}")
    suspend fun updateAddress(
        @Path("addressId") addressId: Long,
        @Header("X-User-Id") userId: Long? = null,
        @Body request: SaveAddressRequestDto
    ): Response<ApiResponse<SavedAddressResponseDto>>

    @DELETE("api/v1/users/me/addresses/{addressId}")
    suspend fun deleteAddress(
        @Path("addressId") addressId: Long,
        @Header("X-User-Id") userId: Long? = null
    ): Response<ApiResponse<Unit>>

    @PATCH("api/v1/users/me/addresses/{addressId}/default")
    suspend fun setDefaultAddress(
        @Path("addressId") addressId: Long,
        @Header("X-User-Id") userId: Long? = null
    ): Response<ApiResponse<SavedAddressResponseDto>>
}
