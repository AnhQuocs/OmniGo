package com.example.omnigo.features.driver.ride.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.omnigo.features.customer.ride.domain.model.Booking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    }

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
            }
        }
    }

    suspend fun clearActiveTrip() {
        _activeBooking.value = null
        dataStore.edit { preferences ->
            preferences.remove(KEY_DRIVER_ACTIVE_BOOKING_ID)
        }
    }
}
