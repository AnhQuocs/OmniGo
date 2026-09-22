package com.example.omnigo.features.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.onboarding.domain.usecase.IsOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isOnboardingCompletedUseCase: IsOnboardingCompletedUseCase,
    private val sessionManager: SessionManager,
    private val locationTracker: LocationTracker,
    private val userLocationManager: UserLocationManager
) : ViewModel() {

    fun decideStartDestination(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val isOnboardingDone = isOnboardingCompletedUseCase()
            if (!isOnboardingDone) {
                onResult("onboarding_root")
                return@launch
            }

            val token = sessionManager.getAccessToken()
            val role = sessionManager.getRole()

            if (!token.isNullOrBlank()) {
                if (role?.equals("DRIVER", ignoreCase = true) == true) {
                    onResult("driver_root")
                } else {
                    val savedLocation = sessionManager.getUserLocation()
                    var isLocationReady = false
                    if (savedLocation != null) {
                        userLocationManager.updateLocation(savedLocation)
                        isLocationReady = true
                    } else if (locationTracker.isLocationPermissionGranted() && locationTracker.isGpsEnabled()) {
                        val current = locationTracker.getCurrentLocation()
                        if (current != null) {
                            userLocationManager.updateLocation(current)
                            sessionManager.saveUserLocation(current)
                            isLocationReady = true
                        }
                    }

                    if (isLocationReady) {
                        onResult("customer_root")
                    } else {
                        onResult("customer_location_setup")
                    }
                }
            } else {
                onResult("auth_root")
            }
        }
    }
}
