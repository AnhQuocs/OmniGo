package com.example.omnigo.features.customer.profile.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.domain.usecase.GetMeUseCase
import com.example.omnigo.features.auth.domain.usecase.LogoutUseCase
import com.example.omnigo.features.auth.domain.usecase.UpdateAvatarUseCase
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class AccountUiState(
    val userName: String = "Bùi Anh Quốc",
    val userPhone: String = "0839072300",
    val userEmail: String = "",
    val userRole: String = "CUSTOMER",
    val avatarUrl: String? = null,
    val vehiclePlate: String? = null,
    val approvalStatus: DriverApprovalStatus = DriverApprovalStatus.APPROVED,
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showAvatarPicker: Boolean = false
)

sealed interface AccountUiEvent {
    data object NavigateToLogin : AccountUiEvent
    data class ShowToast(val message: String) : AccountUiEvent
    data class ShowStringResToast(val resId: Int) : AccountUiEvent
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private var updateAvatarUseCase: UpdateAvatarUseCase? = null
    private var getMeUseCase: GetMeUseCase? = null
    private var getDriverProfileUseCase: GetDriverProfileUseCase? = null

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AccountUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadUserProfile()
    }

    @Inject
    fun injectProfileDependencies(
        updateAvatarUseCase: UpdateAvatarUseCase,
        getMeUseCase: GetMeUseCase,
        driverProfileUseCase: GetDriverProfileUseCase
    ) {
        this.updateAvatarUseCase = updateAvatarUseCase
        this.getMeUseCase = getMeUseCase
        this.getDriverProfileUseCase = driverProfileUseCase
        syncWithRemoteProfile()
        loadDriverProfileInfo()
    }

    fun loadDriverProfileInfo() {
        val useCase = getDriverProfileUseCase ?: return
        viewModelScope.launch {
            val userId = try { sessionManager.getUserId() } catch (_: Exception) { null }
            useCase(userId).onSuccess { driverProfile ->
                _uiState.update {
                    it.copy(
                        userName = driverProfile.fullName?.ifBlank { null } ?: it.userName,
                        userPhone = driverProfile.phoneNumber?.ifBlank { null } ?: it.userPhone,
                        avatarUrl = driverProfile.avatarUrl ?: it.avatarUrl,
                        vehiclePlate = driverProfile.vehiclePlate,
                        approvalStatus = driverProfile.approvalStatus
                    )
                }
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val fullName = sessionManager.getFullName()
            val phone = sessionManager.getPhoneNumber()
            val role = sessionManager.getRole()
            val avatar = try { sessionManager.getAvatarUrl() } catch (_: Exception) { null }

            _uiState.update {
                it.copy(
                    userName = if (!fullName.isNullOrBlank()) fullName else it.userName,
                    userPhone = if (!phone.isNullOrBlank()) phone else it.userPhone,
                    userRole = if (!role.isNullOrBlank()) role else it.userRole,
                    avatarUrl = if (!avatar.isNullOrBlank()) avatar else it.avatarUrl
                )
            }
        }
    }

    fun syncWithRemoteProfile() {
        val useCase = getMeUseCase ?: return
        viewModelScope.launch {
            useCase()
                .catch { /* Network or auth error, fallback to local session */ }
                .collect { user ->
                    _uiState.update {
                        it.copy(
                            userName = if (user.fullName.isNotBlank()) user.fullName else it.userName,
                            userPhone = if (user.phoneNumber.isNotBlank()) user.phoneNumber else it.userPhone,
                            userEmail = user.email.orEmpty(),
                            userRole = user.role.name,
                            avatarUrl = user.avatarUrl ?: it.avatarUrl
                        )
                    }
                    if (!user.avatarUrl.isNullOrBlank()) {
                        try { sessionManager.saveAvatarUrl(user.avatarUrl) } catch (_: Exception) {}
                    }
                }
        }
    }

    fun onAvatarClick() {
        _uiState.update { it.copy(showAvatarPicker = true) }
    }

    fun onDismissAvatarPicker() {
        _uiState.update { it.copy(showAvatarPicker = false) }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, showAvatarPicker = false) }
            val part = ImageCompressor.compressAvatarToMultipart(context, uri)
            performUploadAvatar(part)
        }
    }

    fun uploadAvatarBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, showAvatarPicker = false) }
            val part = ImageCompressor.compressBitmapToMultipart(bitmap)
            performUploadAvatar(part)
        }
    }

    private suspend fun performUploadAvatar(part: MultipartBody.Part?) {
        val useCase = updateAvatarUseCase
        if (part == null || useCase == null) {
            _uiState.update { it.copy(isUploadingAvatar = false) }
            _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_upload_avatar_failed))
            return
        }

        val result = useCase(part)
        result.fold(
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

    fun onLogoutClicked() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onConfirmLogout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showLogoutDialog = false) }
            logoutUseCase()
            _uiState.update { it.copy(isLoading = false) }
            _uiEvent.send(AccountUiEvent.NavigateToLogin)
        }
    }
}
