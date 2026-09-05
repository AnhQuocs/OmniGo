package com.example.omnigo.features.auth.presentation.ui.register

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.auth.presentation.ui.components.LanguageActionMenu
import com.example.omnigo.features.auth.presentation.viewmodel.AuthViewModel
import com.example.omnigo.features.auth.presentation.viewmodel.OtpUiState
import com.example.omnigo.features.auth.presentation.viewmodel.RegisterUiState
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OmniGoTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val otpUiState by authViewModel.otpUiState.collectAsState()
    val registerUiState by authViewModel.registerUiState.collectAsState()

    // Form state
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isPhoneVerified by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Driver specific state
    var selectedVehicleType by remember { mutableStateOf(VehicleType.BIKE) }
    var isVehicleDropdownExpanded by remember { mutableStateOf(false) }
    var licensePlate by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }

    // Resend OTP countdown timer
    var resendCountdown by remember { mutableIntStateOf(0) }

    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000L.milliseconds)
            resendCountdown -= 1
        }
    }

    LaunchedEffect(otpUiState) {
        when (val state = otpUiState) {
            is OtpUiState.CodeSent -> {
                verificationId = state.verificationId
                resendCountdown = 60
            }
            is OtpUiState.AutoVerified, is OtpUiState.Verified -> {
                isPhoneVerified = true
            }
            else -> {}
        }
    }

    LaunchedEffect(registerUiState) {
        if (registerUiState is RegisterUiState.Success) {
            val user = (registerUiState as RegisterUiState.Success).user
            onRegisterSuccess(user)
        }
    }

    OmniGoTheme(darkTheme = false) {
        Scaffold(
            containerColor = BackgroundLight,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingSM)
            ) {
                // Language Switcher Top Bar
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LanguageActionMenu(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Header with OmniGo Logo
                RegisterHeader()

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Error Message Box
                val currentError = when {
                    registerUiState is RegisterUiState.Error -> (registerUiState as RegisterUiState.Error).message.asString()
                    otpUiState is OtpUiState.Error -> (otpUiState as OtpUiState.Error).message.asString()
                    else -> null
                }

                if (currentError != null) {
                    RegisterErrorBanner(errorMessage = currentError)
                    Spacer(modifier = Modifier.height(AppSpacing.M))
                }

                // ---------------- STEP 1: ROLE SELECTION ----------------
                RoleSelectionSection(
                    selectedRole = selectedRole,
                    onRoleSelected = { selectedRole = it }
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // ---------------- STEP 2: PHONE & OTP VERIFICATION ----------------
                PhoneVerificationSection(
                    phoneNumber = phoneNumber,
                    onPhoneChange = { phoneNumber = it },
                    otpCode = otpCode,
                    onOtpChange = { otpCode = it },
                    verificationId = verificationId,
                    isPhoneVerified = isPhoneVerified,
                    isSendingOtp = otpUiState is OtpUiState.Loading,
                    resendCountdown = resendCountdown,
                    onSendOtp = {
                        if (activity != null && phoneNumber.isNotBlank()) {
                            authViewModel.sendOtp(phoneNumber, activity)
                        }
                    },
                    onVerifyOtp = {
                        if (verificationId.isNotBlank() && otpCode.length == 6) {
                            authViewModel.verifyOtp(verificationId, otpCode)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // ---------------- STEP 3: ACCOUNT INFORMATION ----------------
                AccountInfoSection(
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    isPasswordVisible = isPasswordVisible,
                    onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                    isConfirmPasswordVisible = isConfirmPasswordVisible,
                    onToggleConfirmPasswordVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                    isCustomer = selectedRole == UserRole.CUSTOMER,
                    focusManager = focusManager
                )

                // ---------------- DRIVER SPECIFIC FIELDS ----------------
                if (selectedRole == UserRole.DRIVER) {
                    Spacer(modifier = Modifier.height(AppSpacing.L))

                    DriverInfoSection(
                        selectedVehicleType = selectedVehicleType,
                        onVehicleTypeSelected = { selectedVehicleType = it },
                        isDropdownExpanded = isVehicleDropdownExpanded,
                        onDropdownExpandedChange = { isVehicleDropdownExpanded = it },
                        licensePlate = licensePlate,
                        onLicensePlateChange = { licensePlate = it },
                        vehicleModel = vehicleModel,
                        onVehicleModelChange = { vehicleModel = it },
                        focusManager = focusManager
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XL))

                // ---------------- COMPLETE REGISTRATION BUTTON ----------------
                val isRegistering = registerUiState is RegisterUiState.Loading

                val isCustomerFormValid = isPhoneVerified &&
                        fullName.isNotBlank() &&
                        password.isNotBlank() &&
                        password == confirmPassword

                val isDriverFormValid = isCustomerFormValid &&
                        licensePlate.isNotBlank() &&
                        vehicleModel.isNotBlank()

                val isSubmitEnabled = if (selectedRole == UserRole.CUSTOMER) isCustomerFormValid else isDriverFormValid

                RegisterSubmitButton(
                    isLoading = isRegistering,
                    isEnabled = isSubmitEnabled,
                    onClick = {
                        focusManager.clearFocus()
                        if (selectedRole == UserRole.CUSTOMER) {
                            val user = RegisterUser(
                                email = email,
                                password = password,
                                fullName = fullName,
                                phoneNumber = phoneNumber
                            )
                            authViewModel.registerCustomer(user)
                        } else {
                            val driver = RegisterDriver(
                                user = RegisterUser(
                                    email = email,
                                    password = password,
                                    fullName = fullName,
                                    phoneNumber = phoneNumber
                                ),
                                vehicleType = selectedVehicleType,
                                licensePlate = licensePlate,
                                vehicleModel = vehicleModel
                            )
                            authViewModel.registerDriver(driver)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Back to Log-in link
                RegisterFooter(onNavigateToLogin = onNavigateToLogin)

                Spacer(modifier = Modifier.height(AppSpacing.L))
            }
        }
    }
}
