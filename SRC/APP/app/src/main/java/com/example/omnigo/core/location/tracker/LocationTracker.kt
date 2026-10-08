package com.example.omnigo.core.location.tracker

import com.example.omnigo.core.location.model.UserLocation

interface LocationTracker {
    fun isLocationPermissionGranted(): Boolean
    fun isGpsEnabled(): Boolean
    suspend fun getCurrentLocation(): UserLocation?
    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String?
    suspend fun getCoordinatesFromAddress(address: String): Pair<Double, Double>?
}
