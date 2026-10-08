package com.example.omnigo.features.driver.home.presentation.viewmodel

import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus

data class DriverHomeUiState(
    val isOnline: Boolean = false,
    val approvalStatus: DriverApprovalStatus = DriverApprovalStatus.APPROVED,
    val profile: DriverAccountProfile? = null,
    val isLoading: Boolean = false,
    val isStatusUpdating: Boolean = false,
    val errorMessage: String? = null,
    val showApprovalWarningDialog: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val isGpsEnabled: Boolean = false
)
