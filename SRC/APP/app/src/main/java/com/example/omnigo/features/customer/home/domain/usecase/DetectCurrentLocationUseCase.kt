package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.location.tracker.LocationTracker
import javax.inject.Inject

sealed interface DetectLocationResult {
    data class Success(val location: UserLocation) : DetectLocationResult
    data object PermissionRequired : DetectLocationResult
    data object GpsDisabled : DetectLocationResult
    data object LocationUnavailable : DetectLocationResult
}

class DetectCurrentLocationUseCase @Inject constructor(
    private val locationTracker: LocationTracker
) {
    suspend operator fun invoke(): DetectLocationResult {
        if (!locationTracker.isLocationPermissionGranted()) {
            return DetectLocationResult.PermissionRequired
        }

        if (!locationTracker.isGpsEnabled()) {
            return DetectLocationResult.GpsDisabled
        }

        val location = locationTracker.getCurrentLocation()
        return if (location != null) {
            DetectLocationResult.Success(location)
        } else {
            DetectLocationResult.LocationUnavailable
        }
    }
}
