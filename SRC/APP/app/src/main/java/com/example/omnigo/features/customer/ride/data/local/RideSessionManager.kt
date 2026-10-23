package com.example.omnigo.features.customer.ride.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_ACTIVE_BOOKING_ID = longPreferencesKey("active_ride_booking_id")
    }

    val activeBookingIdFlow: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            val id = preferences[KEY_ACTIVE_BOOKING_ID]
            if (id != null && id > 0L) id else null
        }

    suspend fun saveActiveBookingId(bookingId: Long) {
        if (bookingId <= 0L) return
        dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_BOOKING_ID] = bookingId
        }
    }

    suspend fun clearActiveBookingId() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACTIVE_BOOKING_ID)
        }
    }
}
