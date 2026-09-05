package com.example.omnigo.features.auth.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.auth.domain.error.LoginError
import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.LoginResult
import com.example.omnigo.features.auth.domain.model.LogoutResult
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.SendOtpResult
import com.example.omnigo.features.auth.domain.model.VerifyOtpResult
import com.example.omnigo.features.auth.domain.usecase.LoginUseCase
import com.example.omnigo.features.auth.domain.usecase.LogoutUseCase
import com.example.omnigo.features.auth.domain.usecase.RegisterCustomerUseCase
import com.example.omnigo.features.auth.domain.usecase.RegisterDriverUseCase
import com.example.omnigo.features.auth.domain.usecase.RegisterResult
import com.example.omnigo.features.auth.domain.usecase.SendOtpUseCase
import com.example.omnigo.features.auth.domain.usecase.VerifyOtpUseCase
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OtpUiState {
    data object Idle : OtpUiState
    data object Loading : OtpUiState
    data class CodeSent(val verificationId: String) : OtpUiState
    data class AutoVerified(val firebaseToken: String) : OtpUiState
    data class Verified(val firebaseToken: String) : OtpUiState
    data class Error(val message: UiText, val error: RegisterError? = null) : OtpUiState
}

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data class Success(val user: AuthUser) : RegisterUiState
    data class Error(val message: UiText, val error: RegisterError? = null) : RegisterUiState
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val user: AuthUser) : LoginUiState
    data class Error(val message: UiText, val error: LoginError? = null) : LoginUiState
}

sealed interface LogoutUiState {
    data object Idle : LogoutUiState
    data object Loading : LogoutUiState
    data object Success : LogoutUiState
    data class Error(val message: UiText) : LogoutUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val registerCustomerUseCase: RegisterCustomerUseCase,
    private val registerDriverUseCase: RegisterDriverUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _otpUiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val otpUiState: StateFlow<OtpUiState> = _otpUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _logoutUiState = MutableStateFlow<LogoutUiState>(LogoutUiState.Idle)
    val logoutUiState: StateFlow<LogoutUiState> = _logoutUiState.asStateFlow()

    fun sendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _otpUiState.value = OtpUiState.Loading
            sendOtpUseCase(phoneNumber, activity).collect { result ->
                when (result) {
                    is SendOtpResult.CodeSent -> {
                        _otpUiState.value = OtpUiState.CodeSent(result.verificationId)
                    }
                    is SendOtpResult.AutoVerified -> {
                        _otpUiState.value = OtpUiState.AutoVerified(result.firebaseToken)
                    }
                    is SendOtpResult.Error -> {
                        _otpUiState.value = OtpUiState.Error(
                            message = result.error.asUiText(),
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    fun verifyOtp(verificationId: String, otpCode: String) {
        viewModelScope.launch {
            _otpUiState.value = OtpUiState.Loading
            when (val result = verifyOtpUseCase(verificationId, otpCode)) {
                is VerifyOtpResult.Success -> {
                    _otpUiState.value = OtpUiState.Verified(result.firebaseToken)
                }
                is VerifyOtpResult.Error -> {
                    _otpUiState.value = OtpUiState.Error(
                        message = result.error.asUiText(),
                        error = result.error
                    )
                }
            }
        }
    }

    fun registerCustomer(user: RegisterUser) {
        viewModelScope.launch {
            _registerUiState.value = RegisterUiState.Loading
            when (val result = registerCustomerUseCase(user)) {
                is RegisterResult.Success -> {
                    _registerUiState.value = RegisterUiState.Success(result.user)
                }
                is RegisterResult.Error -> {
                    _registerUiState.value = RegisterUiState.Error(
                        message = result.error.asUiText(),
                        error = result.error
                    )
                }
            }
        }
    }

    fun registerDriver(driver: RegisterDriver) {
        viewModelScope.launch {
            _registerUiState.value = RegisterUiState.Loading
            when (val result = registerDriverUseCase(driver)) {
                is RegisterResult.Success -> {
                    _registerUiState.value = RegisterUiState.Success(result.user)
                }
                is RegisterResult.Error -> {
                    _registerUiState.value = RegisterUiState.Error(
                        message = result.error.asUiText(),
                        error = result.error
                    )
                }
            }
        }
    }

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            when (val result = loginUseCase(phoneNumber, password)) {
                is LoginResult.Success -> {
                    _loginUiState.value = LoginUiState.Success(result.user)
                }
                is LoginResult.Error -> {
                    _loginUiState.value = LoginUiState.Error(
                        message = result.error.asUiText(),
                        error = result.error
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutUiState.value = LogoutUiState.Loading
            when (val result = logoutUseCase()) {
                is LogoutResult.Success -> {
                    _logoutUiState.value = LogoutUiState.Success
                    // Reset all other states
                    _loginUiState.value = LoginUiState.Idle
                    _registerUiState.value = RegisterUiState.Idle
                    _otpUiState.value = OtpUiState.Idle
                }
                is LogoutResult.Error -> {
                    _logoutUiState.value = LogoutUiState.Error(result.message)
                }
            }
        }
    }

    fun resetOtpState() {
        _otpUiState.value = OtpUiState.Idle
    }

    fun resetRegisterState() {
        _registerUiState.value = RegisterUiState.Idle
    }

    fun resetLoginState() {
        _loginUiState.value = LoginUiState.Idle
    }

    fun resetLogoutState() {
        _logoutUiState.value = LogoutUiState.Idle
    }
}