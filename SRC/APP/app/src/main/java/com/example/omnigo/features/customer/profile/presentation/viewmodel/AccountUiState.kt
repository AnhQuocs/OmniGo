package com.example.omnigo.features.customer.profile.presentation.viewmodel

import com.example.omnigo.features.customer.address.domain.model.SavedAddress
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus

data class AccountUiState(
    val userName: String = "Nguyễn Văn A",
    val userPhone: String = "0839072300",
    val userEmail: String = "",
    val userRole: String = "CUSTOMER",
    val avatarUrl: String? = null,
    val driverId: Long = 0L,
    val vehiclePlate: String? = null,
    val vehicleType: String = "MOTORBIKE",
    val vehicleBrand: String = "Honda",
    val licenseNumber: String = "",
    val approvalStatus: DriverApprovalStatus = DriverApprovalStatus.APPROVED,
    val rating: Double = 4.9,
    val totalTrips: Int = 256,
    val joinedMonths: Int = 0,
    val joinedDays: Long = 0,
    val registrationDate: String = "15/04/2024",
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeactivateDialog: Boolean = false,
    val showAvatarPicker: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val changePasswordError: String? = null,
    val savedAddresses: List<SavedAddress> = emptyList(),
    val showSavedAddressesSheet: Boolean = false,
    val isLoadingSavedAddresses: Boolean = false,
    val showEditProfileSheet: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val editProfileError: String? = null,
    val showUpdateVehicleDialog: Boolean = false,
    val showResubmitSheet: Boolean = false,
    val isUpdatingDriverVehicle: Boolean = false
)

sealed interface AccountUiEvent {
    data object NavigateToLogin : AccountUiEvent
    data class ShowToast(val message: String) : AccountUiEvent
    data class ShowStringResToast(val resId: Int) : AccountUiEvent
}
