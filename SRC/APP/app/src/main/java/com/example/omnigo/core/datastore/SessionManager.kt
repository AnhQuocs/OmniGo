package com.example.omnigo.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "user_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_ROLE = stringPreferencesKey("user_role")
    }

    private val dataStore = context.sessionDataStore

    val accessTokenFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences -> preferences[KEY_ACCESS_TOKEN] }

    val refreshTokenFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences -> preferences[KEY_REFRESH_TOKEN] }

    val isLoggedInFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences -> !preferences[KEY_ACCESS_TOKEN].isNullOrBlank() }

    suspend fun getAccessToken(): String? {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[KEY_ACCESS_TOKEN] }
            .firstOrNull()
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[KEY_REFRESH_TOKEN] }
            .firstOrNull()
    }

    suspend fun saveSession(
        accessToken: String?,
        refreshToken: String?,
        userId: Long? = null,
        phoneNumber: String? = null,
        fullName: String? = null,
        role: String? = null
    ) {
        dataStore.edit { preferences ->
            if (accessToken != null) preferences[KEY_ACCESS_TOKEN] = accessToken
            if (refreshToken != null) preferences[KEY_REFRESH_TOKEN] = refreshToken
            if (userId != null) preferences[KEY_USER_ID] = userId
            if (phoneNumber != null) preferences[KEY_PHONE_NUMBER] = phoneNumber
            if (fullName != null) preferences[KEY_FULL_NAME] = fullName
            if (role != null) preferences[KEY_ROLE] = role
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
