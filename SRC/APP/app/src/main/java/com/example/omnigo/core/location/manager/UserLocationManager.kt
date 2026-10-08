package com.example.omnigo.core.location.manager

import com.example.omnigo.core.location.model.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocationManager @Inject constructor() {

    private val _currentLocation = MutableStateFlow<UserLocation?>(null)
    val currentLocation: StateFlow<UserLocation?> = _currentLocation.asStateFlow()

    fun updateLocation(location: UserLocation) {
        _currentLocation.value = location
    }

    fun clearLocation() {
        _currentLocation.value = null
    }
}
