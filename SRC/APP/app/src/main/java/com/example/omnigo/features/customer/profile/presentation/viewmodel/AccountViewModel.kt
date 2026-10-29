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
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val getMeUseCase: GetMeUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AccountUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadUserProfile()
        syncWithRemoteProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val fullName = sessionManager.getFullName()
            val phone = sessionManager.getPhoneNumber()
            val role = sessionManager.getRole()
            val avatar = sessionManager.getAvatarUrl()

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
        viewModelScope.launch {
            getMeUseCase()
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
                        sessionManager.saveAvatarUrl(user.avatarUrl)
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
        if (part == null) {
            _uiState.update { it.copy(isUploadingAvatar = false) }
            _uiEvent.send(AccountUiEvent.ShowStringResToast(R.string.profile_upload_avatar_failed))
            return
        }

        val result = updateAvatarUseCase(part)
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
