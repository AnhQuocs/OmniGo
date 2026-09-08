package com.example.omnigo.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val userName: String = "Bùi Anh Quốc",
    val userPhone: String = "0839072300",
    val userRole: String = "CUSTOMER",
    val isLoading: Boolean = false,
    val showLogoutDialog: Boolean = false
)

sealed interface AccountUiEvent {
    data object NavigateToLogin : AccountUiEvent
    data class ShowToast(val message: String) : AccountUiEvent
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AccountUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val fullName = sessionManager.getFullName()
            val phone = sessionManager.getPhoneNumber()
            val role = sessionManager.getRole()

            _uiState.update {
                it.copy(
                    userName = if (!fullName.isNullOrBlank()) fullName else it.userName,
                    userPhone = if (!phone.isNullOrBlank()) phone else it.userPhone,
                    userRole = if (!role.isNullOrBlank()) role else it.userRole
                )
            }
        }
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
