package com.example.omnigo.features.driver.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.UpdateDriverStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverHomeViewModel @Inject constructor(
    private val getDriverProfileUseCase: GetDriverProfileUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val locationTracker: LocationTracker,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState.asStateFlow()

    init {
        loadDriverProfile()
        checkLocationPrerequisites()
    }

    fun loadDriverProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = sessionManager.getUserId()
            val result = getDriverProfileUseCase(userId)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        profile = profile,
                        approvalStatus = profile.approvalStatus,
                        isOnline = profile.isOnline,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }

    fun checkLocationPrerequisites() {
        val hasPermission = locationTracker.isLocationPermissionGranted()
        val isGpsOn = locationTracker.isGpsEnabled()
        _uiState.update {
            it.copy(
                isLocationPermissionGranted = hasPermission,
                isGpsEnabled = isGpsOn
            )
        }
    }

    fun toggleOnlineStatus(targetOnline: Boolean) {
        if (targetOnline) {
            val currentApproval = _uiState.value.approvalStatus
            if (currentApproval != DriverApprovalStatus.APPROVED) {
                _uiState.update { it.copy(showApprovalWarningDialog = true) }
                return
            }

            checkLocationPrerequisites()
            if (!_uiState.value.isLocationPermissionGranted) {
                _uiState.update { it.copy(errorMessage = ERROR_LOCATION_PERMISSION_REQUIRED) }
                return
            }

            if (!_uiState.value.isGpsEnabled) {
                _uiState.update { it.copy(errorMessage = ERROR_GPS_REQUIRED) }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isStatusUpdating = true, errorMessage = null) }
            val driverId = _uiState.value.profile?.driverId ?: sessionManager.getUserId()
            val result = updateDriverStatusUseCase(driverId, targetOnline)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isOnline = targetOnline,
                        isStatusUpdating = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isStatusUpdating = false,
                        errorMessage = error.message ?: "Failed to update driver status"
                    )
                }
            }
        }
    }

    fun dismissApprovalWarningDialog() {
        _uiState.update { it.copy(showApprovalWarningDialog = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        const val ERROR_LOCATION_PERMISSION_REQUIRED = "ERROR_LOCATION_PERMISSION_REQUIRED"
        const val ERROR_GPS_REQUIRED = "ERROR_GPS_REQUIRED"
    }
}
