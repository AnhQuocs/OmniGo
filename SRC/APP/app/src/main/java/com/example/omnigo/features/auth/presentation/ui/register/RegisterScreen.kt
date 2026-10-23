package com.example.omnigo.features.auth.presentation.ui.register

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.auth.presentation.ui.components.LanguageActionMenu
import com.example.omnigo.features.auth.presentation.ui.register.components.DriverPendingApprovalScreen
import com.example.omnigo.features.auth.presentation.ui.register.components.RegisterNavigationActions
import com.example.omnigo.features.auth.presentation.ui.register.components.RegisterStepContent
import com.example.omnigo.features.auth.presentation.ui.register.components.RegisterStepperHeader
import com.example.omnigo.features.auth.presentation.ui.register.components.rememberDriverDocsState
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
    val scope = rememberCoroutineScope()

    val otpUiState by authViewModel.otpUiState.collectAsState()
    val registerUiState by authViewModel.registerUiState.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }
    var showDriverPendingScreen by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isPhoneVerified by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableIntStateOf(0) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var selectedVehicleType by remember { mutableStateOf(VehicleType.BIKE) }
    var isVehicleDropdownExpanded by remember { mutableStateOf(false) }
    var licensePlate by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }

    val docsState = rememberDriverDocsState(context = context, authViewModel = authViewModel, scope = scope)

    val stepTitles = if (selectedRole == UserRole.CUSTOMER) {
        listOf(
            stringResource(id = R.string.register_step_role),
            stringResource(id = R.string.register_step_phone),
            stringResource(id = R.string.register_step_account)
        )
    } else {
        listOf(
            stringResource(id = R.string.register_step_role),
            stringResource(id = R.string.register_step_phone),
            stringResource(id = R.string.register_step_account),
            stringResource(id = R.string.register_step_vehicle),
            stringResource(id = R.string.register_step_docs)
        )
    }
    val totalSteps = stepTitles.size

    BackHandler {
        when {
            showDriverPendingScreen -> onNavigateToLogin()
            currentStep > 0 -> currentStep -= 1
            else -> onNavigateToLogin()
        }
    }

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
            if (selectedRole == UserRole.CUSTOMER) {
                onRegisterSuccess(user)
            } else {
                showDriverPendingScreen = true
            }
        }
    }

    if (showDriverPendingScreen) {
        DriverPendingApprovalScreen(onNavigateToLogin = onNavigateToLogin)
        return
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    LanguageActionMenu(modifier = Modifier.align(Alignment.CenterEnd))
                }
                Spacer(modifier = Modifier.height(AppSpacing.S))

                RegisterHeader()
                Spacer(modifier = Modifier.height(AppSpacing.M))

                RegisterStepperHeader(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    stepTitles = stepTitles
                )
                Spacer(modifier = Modifier.height(AppSpacing.L))

                val currentError = when {
                    registerUiState is RegisterUiState.Error -> (registerUiState as RegisterUiState.Error).message.asString()
                    otpUiState is OtpUiState.Error -> (otpUiState as OtpUiState.Error).message.asString()
                    else -> null
                }
                if (currentError != null) {
                    RegisterErrorBanner(errorMessage = currentError)
                    Spacer(modifier = Modifier.height(AppSpacing.M))
                }

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "step_content"
                ) { step ->
                    RegisterStepContent(
                        step = step,
                        selectedRole = selectedRole,
                        onRoleSelected = {
                            selectedRole = it
                            currentStep = 0
                        },
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
                        },
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
                        focusManager = focusManager,
                        selectedVehicleType = selectedVehicleType,
                        onVehicleTypeSelected = { selectedVehicleType = it },
                        isVehicleDropdownExpanded = isVehicleDropdownExpanded,
                        onDropdownExpandedChange = { isVehicleDropdownExpanded = it },
                        licensePlate = licensePlate,
                        onLicensePlateChange = { licensePlate = it },
                        vehicleModel = vehicleModel,
                        onVehicleModelChange = { vehicleModel = it },
                        docsState = docsState
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XL))

                val isRegistering = registerUiState is RegisterUiState.Loading
                val isLastStep = currentStep == totalSteps - 1

                val isCurrentStepValid = when (currentStep) {
                    0 -> true
                    1 -> isPhoneVerified
                    2 -> fullName.isNotBlank() && password.length >= 6 && password == confirmPassword &&
                            (selectedRole == UserRole.CUSTOMER || email.isNotBlank())
                    3 -> licensePlate.isNotBlank() && vehicleModel.isNotBlank()
                    4 -> docsState.isDocumentsUploaded
                    else -> false
                }

                RegisterNavigationActions(
                    currentStep = currentStep,
                    isLastStep = isLastStep,
                    isCurrentStepValid = isCurrentStepValid,
                    isRegistering = isRegistering,
                    onPreviousStep = { currentStep -= 1 },
                    onNextOrSubmit = {
                        focusManager.clearFocus()
                        if (!isLastStep) {
                            currentStep += 1
                        } else {
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
                                    vehicleModel = vehicleModel,
                                    cccdNumber = docsState.cccdNumber,
                                    cccdFrontImage = docsState.cccdFrontUrl.orEmpty(),
                                    cccdBackImage = docsState.cccdBackUrl.orEmpty(),
                                    gplxNumber = docsState.gplxNumber,
                                    gplxFrontImage = docsState.gplxFrontUrl.orEmpty(),
                                    gplxBackImage = docsState.gplxBackUrl.orEmpty()
                                )
                                authViewModel.registerDriver(driver)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                RegisterFooter(onNavigateToLogin = onNavigateToLogin)

                Spacer(modifier = Modifier.height(AppSpacing.L))
            }
        }
    }
}
