package com.example.omnigo.features.auth.presentation.ui.login

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.presentation.ui.components.LanguageActionMenu
import com.example.omnigo.features.auth.presentation.viewmodel.AuthViewModel
import com.example.omnigo.features.auth.presentation.viewmodel.LoginUiState
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OmniGoTheme

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val loginUiState by authViewModel.loginUiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(loginUiState) {
        if (loginUiState is LoginUiState.Success) {
            val user = (loginUiState as LoginUiState.Success).user
            onLoginSuccess(user)
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

                Spacer(modifier = Modifier.height(AppSpacing.L))

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
}
