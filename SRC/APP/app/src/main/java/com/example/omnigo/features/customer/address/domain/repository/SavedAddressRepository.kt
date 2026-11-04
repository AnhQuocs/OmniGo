package com.example.omnigo.features.customer.address.domain.repository

import com.example.omnigo.features.customer.address.domain.model.SavedAddress

interface SavedAddressRepository {
    suspend fun getSavedAddresses(): Result<List<SavedAddress>>
    suspend fun saveAddress(
        label: String,
        address: String,
        latitude: Double,
        longitude: Double,
        isDefault: Boolean = false
    ): Result<SavedAddress>
    suspend fun updateAddress(
        addressId: Long,
        label: String,
        address: String,
        latitude: Double,
        longitude: Double,
        isDefault: Boolean = false
    ): Result<SavedAddress> = Result.failure(UnsupportedOperationException())
    suspend fun deleteAddress(addressId: Long): Result<Unit>
    suspend fun setDefaultAddress(addressId: Long): Result<SavedAddress>
}
