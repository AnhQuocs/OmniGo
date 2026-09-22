package com.example.omnigo.features.customer.home.domain.repository

import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import kotlinx.coroutines.flow.Flow

interface SavedAddressRepository {
    fun getSavedAddresses(): Flow<List<SavedAddress>>
    suspend fun saveAddress(address: SavedAddress)
    suspend fun updateAddress(address: SavedAddress)
    suspend fun deleteAddress(addressId: String)
}
