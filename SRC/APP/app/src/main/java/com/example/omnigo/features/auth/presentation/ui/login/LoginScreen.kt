package com.example.omnigo.features.auth.presentation.ui.login

import com.example.omnigo.utils.showToast
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.presentation.ui.components.LanguageActionMenu
import com.example.omnigo.features.auth.presentation.viewmodel.AuthViewModel
import com.example.omnigo.features.auth.presentation.viewmodel.LoginUiState
import com.example.omnigo.features.auth.presentation.viewmodel.ResetPasswordUiState
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OmniGoTheme
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val loginUiState by authViewModel.loginUiState.collectAsState()
    val resetPasswordUiState by authViewModel.resetPasswordUiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(loginUiState) {
        if (loginUiState is LoginUiState.Success) {
            val user = (loginUiState as LoginUiState.Success).user
            onLoginSuccess(user)
        }
    }

    LaunchedEffect(resetPasswordUiState) {
        if (resetPasswordUiState is ResetPasswordUiState.Success) {
            context.showToast(R.string.auth_reset_password_success)
            showForgotPasswordDialog = false
            authViewModel.resetResetPasswordState()
        }
    }

    OmniGoTheme(darkTheme = false) {
        Scaffold(
            containerColor = BackgroundLight,
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Language Switcher Top Bar
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LanguageActionMenu(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Brand Header
                LoginHeader()

                Spacer(modifier = Modifier.height(AppSpacing.XL))

                // Error Message Box
                if (loginUiState is LoginUiState.Error) {
                    val errorMessage = (loginUiState as LoginUiState.Error).message.asString()
                    LoginErrorBanner(errorMessage = errorMessage)
                    Spacer(modifier = Modifier.height(AppSpacing.M))
                }

                // Phone Input Field
                LoginPhoneField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Password Input Field
                LoginPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    isVisible = isPasswordVisible,
                    onVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                    onDone = {
                        focusManager.clearFocus()
                        if (phoneNumber.isNotBlank() && password.isNotBlank()) {
                            authViewModel.login(phoneNumber, password)
                        }
                    }
                )

                // Forgot Password Link
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text(
                            text = stringResource(id = R.string.forgot_password),
                            style = MaterialTheme.typography.s14.semiBold(),
                            color = PrimaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Submit Login Button
                val isLoading = loginUiState is LoginUiState.Loading
                val isFormValid = phoneNumber.isNotBlank() && password.isNotBlank()

                LoginSubmitButton(
                    isLoading = isLoading,
                    isEnabled = isFormValid,
                    onClick = {
                        focusManager.clearFocus()
                        authViewModel.login(phoneNumber, password)
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.XL))

                // Register Link Footer
                LoginFooter(
                    onNavigateToRegister = {
                        authViewModel.resetLoginState()
                        onNavigateToRegister()
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))
            }
        }
    }

    if (showForgotPasswordDialog) {
        val isResetLoading = resetPasswordUiState is ResetPasswordUiState.Loading
        val isOtpSent = resetPasswordUiState is ResetPasswordUiState.OtpSent
        val resetError = (resetPasswordUiState as? ResetPasswordUiState.Error)?.message
        ForgotPasswordDialog(
            initialPhoneNumber = phoneNumber,
            isLoading = isResetLoading,
            errorMessage = resetError,
            isOtpSent = isOtpSent,
            onSendOtp = { phone ->
                authViewModel.sendPasswordResetOtp(phone)
            },
            onDismiss = {
                showForgotPasswordDialog = false
                authViewModel.resetResetPasswordState()
            },
            onSubmit = { phone, newPass, confirmPass, otpCode ->
                authViewModel.resetPassword(phone, newPass, confirmPass, otpCode)
            }
        )
    }
}
