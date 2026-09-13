package com.example.omnigo.features.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.onboarding.domain.usecase.IsOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isOnboardingCompletedUseCase: IsOnboardingCompletedUseCase,
    private val sessionManager: SessionManager
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
                    onResult("customer_root")
                }
            } else {
                onResult("auth_root")
            }
        }
    }
}
