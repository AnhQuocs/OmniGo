package com.example.omnigo.features.profile.presentation.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.language.presentation.ui.ChangeLanguageActivity
import com.example.omnigo.features.profile.presentation.viewmodel.AccountUiEvent
import com.example.omnigo.features.profile.presentation.viewmodel.AccountViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun AccountScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AccountUiEvent.NavigateToLogin -> onNavigateToLogin()
                is AccountUiEvent.ShowToast -> {}
            }
        }
    }

    if (uiState.showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = { viewModel.onConfirmLogout() },
            onDismiss = { viewModel.onDismissLogoutDialog() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .padding(horizontal = Dimen.PaddingM)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(id = R.string.account_title),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary,
            modifier = Modifier.padding(vertical = Dimen.PaddingM)
        )

        AccountHeaderSection(
            userName = uiState.userName,
            userPhone = uiState.userPhone,
            userRole = uiState.userRole
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        AccountWalletCard(
            balance = stringResource(id = R.string.account_wallet_balance_mock),
            onTopUpClick = { /* Top up wallet */ }
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // Menu Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShape.ShapeM),
            color = SurfaceLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
        ) {
            Column {
                AccountMenuItem(
                    icon = Icons.Filled.Bookmark,
                    title = stringResource(id = R.string.account_saved_places),
                    onClick = { /* Navigate to saved places */ }
                )
                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))
                AccountMenuItem(
                    icon = Icons.Filled.Language,
                    title = stringResource(id = R.string.language),
                    onClick = {
                        context.startActivity(Intent(context, ChangeLanguageActivity::class.java))
                    }
                )
                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))
                AccountMenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = stringResource(id = R.string.account_help_center),
                    onClick = { /* Help center */ }
                )
                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))
                AccountMenuItem(
                    icon = Icons.Filled.Policy,
                    title = stringResource(id = R.string.account_terms),
                    onClick = { /* Terms and privacy */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // Logout Button
        Button(
            onClick = { viewModel.onLogoutClicked() },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.HeightDefault),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
            shape = RoundedCornerShape(AppShape.ShapeM),
            enabled = !uiState.isLoading
        ) {
            Text(
                text = stringResource(id = R.string.account_logout),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        Text(
            text = stringResource(id = R.string.account_app_version, "1.0.0"),
            style = MaterialTheme.typography.s12.normal(),
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(AppSpacing.XXL))
    }
}
