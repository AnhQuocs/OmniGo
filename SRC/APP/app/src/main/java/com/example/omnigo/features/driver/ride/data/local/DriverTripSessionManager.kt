package com.example.omnigo.features.driver.ride.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverTripSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_DRIVER_ACTIVE_BOOKING_ID = longPreferencesKey("driver_active_booking_id")
        private val KEY_DRIVER_ACTIVE_BOOKING_JSON = stringPreferencesKey("driver_active_booking_json")
        private val KEY_DRIVER_ACTIVE_BOOKING_SAVED_AT = longPreferencesKey("driver_active_booking_saved_at")
        const val SNAPSHOT_MAX_AGE_MS = 12 * 60 * 60 * 1000L
    }

    private val gson = Gson()

    internal var clock: () -> Long = { System.currentTimeMillis() }

    private val _activeBooking = MutableStateFlow<Booking?>(null)
    val activeBooking: StateFlow<Booking?> = _activeBooking.asStateFlow()

    val activeBookingIdFlow: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            val id = preferences[KEY_DRIVER_ACTIVE_BOOKING_ID]
            if (id != null && id > 0L) id else null
        }

    suspend fun saveActiveTrip(booking: Booking) {
        _activeBooking.value = booking
        if (booking.bookingId > 0L) {
            dataStore.edit { preferences ->
                preferences[KEY_DRIVER_ACTIVE_BOOKING_ID] = booking.bookingId
                preferences[KEY_DRIVER_ACTIVE_BOOKING_JSON] = gson.toJson(booking)
                preferences[KEY_DRIVER_ACTIVE_BOOKING_SAVED_AT] = clock()
            }
        }
    }

    /**
     * Rehydrates the in-memory active booking from the persisted snapshot after a process restart.
     * Returns true only when a booking was actually restored; no-op when a booking is already in memory,
     * when no snapshot exists, or when it is expired/corrupted.
     */
    suspend fun restoreActiveTrip(): Boolean {
        if (_activeBooking.value != null) return false
        val preferences = runCatching { dataStore.data.first() }.getOrNull() ?: return false
        val json = preferences[KEY_DRIVER_ACTIVE_BOOKING_JSON] ?: return false
        val savedAt = preferences[KEY_DRIVER_ACTIVE_BOOKING_SAVED_AT] ?: 0L

        if (clock() - savedAt > SNAPSHOT_MAX_AGE_MS) {
            clearPersistedSnapshot()
            return false
        }

        val booking = runCatching { gson.fromJson(json, Booking::class.java) }.getOrNull()
        if (booking == null || booking.bookingId <= 0L) {
            clearPersistedSnapshot()
            return false
        }
        if (_activeBooking.value != null) return false
        _activeBooking.value = booking
        return true
    }

    suspend fun clearActiveTrip() {
        _activeBooking.value = null
        clearPersistedSnapshot()
    }

    private suspend fun clearPersistedSnapshot() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_DRIVER_ACTIVE_BOOKING_ID)
            preferences.remove(KEY_DRIVER_ACTIVE_BOOKING_JSON)
            preferences.remove(KEY_DRIVER_ACTIVE_BOOKING_SAVED_AT)
        }
    }
}
