package com.example.omnigo.features.customer.address.data.repository

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.network.toBackendMessage
import com.example.omnigo.features.customer.address.data.remote.api.UserAddressApi
import com.example.omnigo.features.customer.address.data.remote.dto.SaveAddressRequestDto
import com.example.omnigo.features.customer.address.data.remote.dto.SavedAddressResponseDto
import com.example.omnigo.features.customer.address.domain.model.SavedAddress
import com.example.omnigo.features.customer.address.domain.repository.SavedAddressRepository
import retrofit2.HttpException
import javax.inject.Inject

class SavedAddressRepositoryImpl @Inject constructor(
    private val addressApi: UserAddressApi,
    private val sessionManager: SessionManager
) : SavedAddressRepository {

    override suspend fun getSavedAddresses(): Result<List<SavedAddress>> = runCatching {
        val userId = sessionManager.getUserId()
        val response = addressApi.getSavedAddresses(userId = userId)
        if (!response.isSuccessful) throw HttpException(response)
        val dtos = response.body()?.data ?: emptyList()
        dtos.map { it.toDomain() }
    }.recoverCatching { err ->
        val msg = if (err is HttpException) err.toBackendMessage() ?: err.message() else err.message ?: "Lỗi tải địa chỉ"
        throw Exception(msg)
    }

    override suspend fun saveAddress(
        label: String,
        address: String,
        latitude: Double,
        longitude: Double,
        isDefault: Boolean
    ): Result<SavedAddress> = runCatching {
        val userId = sessionManager.getUserId()
        val req = SaveAddressRequestDto(
            label = label,
            address = address,
            latitude = latitude,
            longitude = longitude,
            isDefault = isDefault
        )
        val response = addressApi.saveAddress(userId = userId, request = req)
        if (!response.isSuccessful) throw HttpException(response)
        val dto = response.body()?.data ?: throw Exception("Không có dữ liệu trả về")
        dto.toDomain()
    }.recoverCatching { err ->
        val msg = if (err is HttpException) err.toBackendMessage() ?: err.message() else err.message ?: "Lỗi lưu địa chỉ"
        throw Exception(msg)
    }

    override suspend fun updateAddress(
        addressId: Long,
        label: String,
        address: String,
        latitude: Double,
        longitude: Double,
        isDefault: Boolean
    ): Result<SavedAddress> = runCatching {
        val userId = sessionManager.getUserId()
        val req = SaveAddressRequestDto(
            label = label,
            address = address,
            latitude = latitude,
            longitude = longitude,
            isDefault = isDefault
        )
        val response = addressApi.updateAddress(addressId = addressId, userId = userId, request = req)
        if (!response.isSuccessful) throw HttpException(response)
        val dto = response.body()?.data ?: throw Exception("Không có dữ liệu trả về")
        dto.toDomain()
    }.recoverCatching { err ->
        val msg = if (err is HttpException) err.toBackendMessage() ?: err.message() else err.message ?: "Lỗi cập nhật địa chỉ"
        throw Exception(msg)
    }

    override suspend fun deleteAddress(addressId: Long): Result<Unit> = runCatching {
        val userId = sessionManager.getUserId()
        val response = addressApi.deleteAddress(addressId = addressId, userId = userId)
        if (!response.isSuccessful) throw HttpException(response)
    }.recoverCatching { err ->
        val msg = if (err is HttpException) err.toBackendMessage() ?: err.message() else err.message ?: "Lỗi xóa địa chỉ"
        throw Exception(msg)
    }

    override suspend fun setDefaultAddress(addressId: Long): Result<SavedAddress> = runCatching {
        val userId = sessionManager.getUserId()
        val response = addressApi.setDefaultAddress(addressId = addressId, userId = userId)
        if (!response.isSuccessful) throw HttpException(response)
        val dto = response.body()?.data ?: throw Exception("Không có dữ liệu trả về")
        dto.toDomain()
    }.recoverCatching { err ->
        val msg = if (err is HttpException) err.toBackendMessage() ?: err.message() else err.message ?: "Lỗi đặt địa chỉ mặc định"
        throw Exception(msg)
    }

    private fun SavedAddressResponseDto.toDomain() = SavedAddress(
        id = id,
        userId = userId,
        label = label,
        addressType = addressType ?: "OTHER",
        address = address,
        latitude = latitude,
        longitude = longitude,
        isDefault = isDefault ?: false
    )
}
