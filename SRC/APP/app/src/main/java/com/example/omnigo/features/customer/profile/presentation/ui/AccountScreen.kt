package com.example.omnigo.features.customer.profile.presentation.ui

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.PersonOutline
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.profile.presentation.ui.components.AvatarPickerBottomSheet
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountUiEvent
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountViewModel
import com.example.omnigo.features.language.presentation.ui.ChangeLanguageActivity
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AccountScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.uploadAvatarBitmap(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.syncWithRemoteProfile()
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AccountUiEvent.NavigateToLogin -> onNavigateToLogin()
                is AccountUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AccountUiEvent.ShowStringResToast -> Toast.makeText(context, context.getString(event.resId), Toast.LENGTH_SHORT).show()
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

        // Hero Header Section
        AccountHeaderSection(
            userName = uiState.userName,
            userPhone = uiState.userPhone,
            userRole = uiState.userRole,
            avatarUrl = uiState.avatarUrl,
            isUploadingAvatar = uiState.isUploadingAvatar,
            onProfileClick = {
                context.startActivity(PersonalProfileActivity.createIntent(context))
            },
            onAvatarClick = { viewModel.onAvatarClick() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Quick Dashboard (OmniPay, Promo, Activity)
        AccountQuickDashboard(
            balance = stringResource(id = R.string.account_wallet_balance_mock),
            onWalletClick = { /* Top up wallet / View balance */ },
            onPromoClick = { /* Promo code / vouchers */ },
            onActivityClick = { /* Recent ride/food activity */ }
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // Group 1: Account & Profile
        Text(
            text = stringResource(id = R.string.profile_section_account),
            style = MaterialTheme.typography.s12.semiBold(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimen.PaddingS, start = Dimen.PaddingXS)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShape.ShapeM),
            color = SurfaceLight,
            border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, CardBorderColor)
        ) {
            Column {
                AccountMenuItem(
                    icon = Icons.Outlined.PersonOutline,
                    title = stringResource(id = R.string.profile_personal_info_title),
                    onClick = {
                        context.startActivity(PersonalProfileActivity.createIntent(context))
                    }
                )
                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.PaddingXXS)
                AccountMenuItem(
                    icon = Icons.Filled.Bookmark,
                    title = stringResource(id = R.string.account_saved_places),
                    onClick = { /* Navigate to saved places */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // Group 2: Settings & Support
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShape.ShapeM),
            color = SurfaceLight,
            border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, CardBorderColor)
        ) {
            Column {
                AccountMenuItem(
                    icon = Icons.Filled.Language,
                    title = stringResource(id = R.string.language),
                    onClick = {
                        context.startActivity(Intent(context, ChangeLanguageActivity::class.java))
                    }
                )
                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.PaddingXXS)
                AccountMenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = stringResource(id = R.string.account_help_center),
                    onClick = { /* Help center */ }
                )
                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.PaddingXXS)
                AccountMenuItem(
                    icon = Icons.Filled.Policy,
                    title = stringResource(id = R.string.account_terms),
                    onClick = {
                        context.startActivity(
                            com.example.omnigo.features.customer.profile.presentation.ui.legal.TermsAndPrivacyActivity.createIntent(context)
                        )
                    }
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
            shape = RoundedCornerShape(AppShape.ShapeM),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
        ) {
            Text(
                text = stringResource(id = R.string.account_logout),
                style = MaterialTheme.typography.s14.bold(),
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // App Version
        Text(
            text = stringResource(id = R.string.account_app_version, "1.0.0"),
            style = MaterialTheme.typography.s12.medium(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = Dimen.PaddingL)
        )
    }

    if (uiState.showAvatarPicker) {
        AvatarPickerBottomSheet(
            hasCurrentAvatar = !uiState.avatarUrl.isNullOrBlank(),
            onDismiss = { viewModel.onDismissAvatarPicker() },
            onTakePhoto = {
                viewModel.onDismissAvatarPicker()
                cameraLauncher.launch(null)
            },
            onChooseGallery = {
                viewModel.onDismissAvatarPicker()
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onViewAvatar = {
                viewModel.onDismissAvatarPicker()
                context.startActivity(PersonalProfileActivity.createIntent(context))
            }
        )
    }
}
