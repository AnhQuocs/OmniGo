package com.example.omnigo.features.customer.home.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedAddressRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) : SavedAddressRepository {

    companion object {
        private val KEY_SAVED_ADDRESSES = stringPreferencesKey("saved_addresses_list_json")
    }

    override fun getSavedAddresses(): Flow<List<SavedAddress>> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { preferences ->
                val json = preferences[KEY_SAVED_ADDRESSES]
                if (json.isNullOrBlank()) {
                    emptyList()
                } else {
                    try {
                        val listType = object : TypeToken<List<SavedAddress>>() {}.type
                        val list: List<SavedAddress> = gson.fromJson(json, listType) ?: emptyList()
                        list.sortedByDescending { it.updatedAt }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
    }

    override suspend fun saveAddress(address: SavedAddress) {
        dataStore.edit { preferences ->
            val json = preferences[KEY_SAVED_ADDRESSES]
            val list = if (json.isNullOrBlank()) {
                mutableListOf()
            } else {
                try {
                    val listType = object : TypeToken<List<SavedAddress>>() {}.type
                    gson.fromJson<List<SavedAddress>>(json, listType)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }
            }

            // Remove existing item with same id or same full address
            list.removeAll { it.id == address.id || it.fullAddress.equals(address.fullAddress, ignoreCase = true) }
            list.add(0, address.copy(updatedAt = System.currentTimeMillis()))

            preferences[KEY_SAVED_ADDRESSES] = gson.toJson(list)
        }
    }

    override suspend fun updateAddress(address: SavedAddress) {
        saveAddress(address)
    }

    override suspend fun deleteAddress(addressId: String) {
        dataStore.edit { preferences ->
            val json = preferences[KEY_SAVED_ADDRESSES] ?: return@edit
            try {
                val listType = object : TypeToken<List<SavedAddress>>() {}.type
                val list: MutableList<SavedAddress> = gson.fromJson<List<SavedAddress>>(json, listType)?.toMutableList() ?: return@edit
                list.removeAll { it.id == addressId }
                preferences[KEY_SAVED_ADDRESSES] = gson.toJson(list)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
