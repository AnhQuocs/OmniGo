package com.example.omnigo.features.customer.profile.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.domain.usecase.ChangePasswordUseCase
import com.example.omnigo.features.auth.domain.usecase.DeleteAccountUseCase
import com.example.omnigo.features.auth.domain.usecase.GetMeUseCase
import com.example.omnigo.features.auth.domain.usecase.LogoutUseCase
import com.example.omnigo.features.auth.domain.usecase.UpdateUserProfileUseCase
import com.example.omnigo.features.customer.address.domain.usecase.DeleteAddressUseCase
import com.example.omnigo.features.customer.address.domain.usecase.GetSavedAddressesUseCase
import com.example.omnigo.features.customer.address.domain.usecase.SaveAddressUseCase
import com.example.omnigo.features.customer.address.domain.usecase.UpdateSavedAddressUseCase
import com.example.omnigo.features.customer.ride.domain.model.GetDriverBookingsResult
import com.example.omnigo.features.driver.domain.usecase.GetDriverBookingsUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.ResubmitDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.UpdateDriverVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager,
    private val getMeUseCase: GetMeUseCase,
    private val avatarHelper: AccountAvatarHelper,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val saveAddressUseCase: SaveAddressUseCase,
    private val updateSavedAddressUseCase: UpdateSavedAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val getDriverProfileUseCase: GetDriverProfileUseCase,
    private val getDriverBookingsUseCase: GetDriverBookingsUseCase,
    private val updateDriverVehicleUseCase: UpdateDriverVehicleUseCase,
    private val resubmitDriverProfileUseCase: ResubmitDriverProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AccountUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadUserProfile()
        syncWithRemoteProfile()
        loadDriverProfileInfo()
        loadDriverBookingsCount()
    }

    fun loadDriverProfileInfo() {
        viewModelScope.launch {
            val userId = runCatching { sessionManager.getUserId() }.getOrNull()
            getDriverProfileUseCase(userId).onSuccess { driverProfile ->
                _uiState.update {
                    it.copy(
                        driverId = driverProfile.driverId,
                        userName = driverProfile.fullName?.ifBlank { null } ?: it.userName,
                        userPhone = driverProfile.phoneNumber?.ifBlank { null } ?: it.userPhone,
                        avatarUrl = driverProfile.avatarUrl ?: it.avatarUrl,
                        vehiclePlate = driverProfile.vehiclePlate ?: it.vehiclePlate,
                        vehicleType = driverProfile.vehicleType ?: it.vehicleType,
                        vehicleBrand = driverProfile.vehicleBrand ?: it.vehicleBrand,
                        licenseNumber = driverProfile.licenseNumber ?: it.licenseNumber,
                        approvalStatus = driverProfile.approvalStatus
                    )
                }
            }
        }
    }

    fun loadDriverBookingsCount() {
        viewModelScope.launch {
            val result = getDriverBookingsUseCase()
            if (result is GetDriverBookingsResult.Success) {
                val completedCount = result.bookings.count { it.status.name.equals("COMPLETED", ignoreCase = true) }
                if (completedCount > 0) {
                    _uiState.update { it.copy(totalTrips = completedCount) }
                }
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    userName = sessionManager.getFullName() ?: it.userName,
                    userPhone = sessionManager.getPhoneNumber() ?: it.userPhone,
                    userRole = sessionManager.getRole() ?: it.userRole,
                    avatarUrl = sessionManager.getAvatarUrl() ?: it.avatarUrl
                )
            }
        }
    }

    fun syncWithRemoteProfile() {
        viewModelScope.launch {
            try {
                val user = getMeUseCase().firstOrNull() ?: return@launch
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formattedDate = runCatching { user.createdAt.format(formatter) }.getOrElse { _uiState.value.registrationDate }
                val now = java.time.LocalDateTime.now()
                val totalDays = java.time.Duration.between(user.createdAt, now).toDays().coerceAtLeast(0)
                val months = (totalDays / 30).toInt().coerceAtLeast(0)

                _uiState.update {
                    it.copy(
                        userName = user.fullName,
                        userPhone = user.phoneNumber,
                        userEmail = user.email.orEmpty(),
                        userRole = user.role.name,
                        avatarUrl = user.avatarUrl ?: it.avatarUrl,
                        registrationDate = formattedDate,
                        joinedDays = totalDays,
                        joinedMonths = months
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun onAvatarClick() = _uiState.update { it.copy(showAvatarPicker = true) }
    fun onDismissAvatarPicker() = _uiState.update { it.copy(showAvatarPicker = false) }
    fun onOpenChangePassword() = _uiState.update { it.copy(showChangePasswordDialog = true, changePasswordError = null) }
    fun onDismissChangePassword() = _uiState.update { it.copy(showChangePasswordDialog = false, changePasswordError = null) }

    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, changePasswordError = null) }
            changePasswordUseCase(oldPass, newPass, confirmPass).fold(
                onSuccess = {
                    _uiState.update { it.copy(isChangingPassword = false, showChangePasswordDialog = false) }
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.account_change_password_success))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isChangingPassword = false, changePasswordError = error.message ?: "Đổi mật khẩu thất bại") }
                }
            )
        }
    }

    fun onOpenEditProfile() = _uiState.update { it.copy(showEditProfileSheet = true, editProfileError = null) }
    fun onDismissEditProfile() = _uiState.update { it.copy(showEditProfileSheet = false, editProfileError = null) }

    fun updateProfile(fullName: String, email: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProfile = true, editProfileError = null) }
            updateUserProfileUseCase(fullName, email).fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            userName = updatedUser.fullName,
                            userEmail = updatedUser.email.orEmpty(),
                            isUpdatingProfile = false,
                            showEditProfileSheet = false,
                            editProfileError = null
                        )
                    }
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_edit_success))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isUpdatingProfile = false, editProfileError = error.message ?: "Cập nhật thông tin thất bại") }
                }
            )
        }
    }

    fun onOpenSavedAddresses() {
        _uiState.update { it.copy(showSavedAddressesSheet = true) }
        loadSavedAddresses()
    }

    fun onDismissSavedAddresses() = _uiState.update { it.copy(showSavedAddressesSheet = false) }

    fun loadSavedAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSavedAddresses = true) }
            getSavedAddressesUseCase().fold(
                onSuccess = { list -> _uiState.update { it.copy(savedAddresses = list, isLoadingSavedAddresses = false) } },
                onFailure = { _uiState.update { it.copy(isLoadingSavedAddresses = false) } }
            )
        }
    }

    fun saveAddress(label: String, addressText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSavedAddresses = true) }
            saveAddressUseCase(label = label, address = addressText, latitude = 10.7769, longitude = 106.7009, isDefault = false).fold(
                onSuccess = {
                    loadSavedAddresses()
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.saved_address_save_success))
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoadingSavedAddresses = false) }
                    _uiEvent.send(AccountUiEvent.ShowToast(err.message ?: "Lỗi lưu địa chỉ"))
                }
            )
        }
    }

    fun updateAddress(addressId: Long, label: String, addressText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSavedAddresses = true) }
            updateSavedAddressUseCase(addressId = addressId, label = label, address = addressText, latitude = 10.7769, longitude = 106.7009).fold(
                onSuccess = {
                    loadSavedAddresses()
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.saved_address_update_success))
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoadingSavedAddresses = false) }
                    _uiEvent.send(AccountUiEvent.ShowToast(err.message ?: "Lỗi cập nhật địa chỉ"))
                }
            )
        }
    }

    fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            deleteAddressUseCase(addressId).fold(
                onSuccess = {
                    loadSavedAddresses()
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.saved_address_delete_success))
                },
                onFailure = { err -> _uiEvent.send(AccountUiEvent.ShowToast(err.message ?: "Lỗi xóa địa chỉ")) }
            )
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, showAvatarPicker = false) }
            avatarHelper.uploadFromUri(context, uri).fold(
                onSuccess = { newUrl ->
                    _uiState.update { it.copy(avatarUrl = newUrl, isUploadingAvatar = false) }
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_upload_avatar_success))
                },
                onFailure = {
                    _uiState.update { it.copy(isUploadingAvatar = false) }
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_upload_avatar_failed))
                }
            )
        }
    }

    fun uploadAvatarBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, showAvatarPicker = false) }
            avatarHelper.uploadFromBitmap(bitmap).fold(
                onSuccess = { newUrl ->
                    _uiState.update { it.copy(avatarUrl = newUrl, isUploadingAvatar = false) }
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_upload_avatar_success))
                },
                onFailure = {
                    _uiState.update { it.copy(isUploadingAvatar = false) }
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_upload_avatar_failed))
                }
            )
        }
    }

    fun onOpenUpdateVehicleDialog() = _uiState.update { it.copy(showUpdateVehicleDialog = true) }
    fun onDismissUpdateVehicleDialog() = _uiState.update { it.copy(showUpdateVehicleDialog = false) }
    fun onOpenResubmitSheet() = _uiState.update { it.copy(showResubmitSheet = true) }
    fun onDismissResubmitSheet() = _uiState.update { it.copy(showResubmitSheet = false) }

    fun updateDriverVehicle(vehicleType: String, licensePlate: String, vehicleBrand: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingDriverVehicle = true) }
            val driverId = _uiState.value.driverId.takeIf { it > 0L }
            updateDriverVehicleUseCase(driverId = driverId, vehicleType = vehicleType, licensePlate = licensePlate, vehicleBrand = vehicleBrand)
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isUpdatingDriverVehicle = false,
                            showUpdateVehicleDialog = false,
                            vehiclePlate = updated.vehiclePlate ?: licensePlate,
                            vehicleType = updated.vehicleType ?: vehicleType,
                            vehicleBrand = updated.vehicleBrand ?: vehicleBrand
                        )
                    }
                    _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.driver_vehicle_update_success))
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isUpdatingDriverVehicle = false) }
                    _uiEvent.send(AccountUiEvent.ShowToast(err.message ?: "Không thể cập nhật xe"))
                }
        }
    }

    fun resubmitDriverProfile(
        fullName: String,
        licenseNumber: String,
        vehicleType: String,
        licensePlate: String,
        vehicleBrand: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingDriverVehicle = true) }
            val driverId = _uiState.value.driverId.takeIf { it > 0L }
            resubmitDriverProfileUseCase(
                driverId = driverId,
                fullName = fullName,
                email = _uiState.value.userEmail.ifBlank { null },
                licenseNumber = licenseNumber,
                vehicleType = vehicleType,
                licensePlate = licensePlate,
                vehicleBrand = vehicleBrand
            ).onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        isUpdatingDriverVehicle = false,
                        showResubmitSheet = false,
                        approvalStatus = updated.approvalStatus,
                        vehiclePlate = updated.vehiclePlate ?: licensePlate,
                        vehicleType = updated.vehicleType ?: vehicleType,
                        vehicleBrand = updated.vehicleBrand ?: vehicleBrand,
                        licenseNumber = updated.licenseNumber ?: licenseNumber
                    )
                }
                _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.driver_resubmit_success))
            }.onFailure { err ->
                _uiState.update { it.copy(isUpdatingDriverVehicle = false) }
                _uiEvent.send(AccountUiEvent.ShowToast(err.message ?: "Nộp lại hồ sơ thất bại"))
            }
        }
    }

    fun onOpenDeactivateDialog() = _uiState.update { it.copy(showDeactivateDialog = true) }
    fun onDismissDeactivateDialog() = _uiState.update { it.copy(showDeactivateDialog = false) }

    fun onConfirmDeactivateAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeactivateDialog = false) }
            deleteAccountUseCase().onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_deactivate_success))
                _uiEvent.send(AccountUiEvent.NavigateToLogin)
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(AccountUiEvent.ShowToast(err.message ?: "Không thể vô hiệu hóa tài khoản"))
            }
        }
    }

    fun onLogoutClicked() = _uiState.update { it.copy(showLogoutDialog = true) }
    fun onDismissLogoutDialog() = _uiState.update { it.copy(showLogoutDialog = false) }

    fun onConfirmLogout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showLogoutDialog = false) }
            logoutUseCase()
            _uiState.update { it.copy(isLoading = false) }
            _uiEvent.send(AccountUiEvent.NavigateToLogin)
        }
    }
}
