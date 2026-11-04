package com.example.omnigo.features.customer.profile.presentation.ui

import com.example.omnigo.utils.showToast
import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.address.presentation.ui.SavedAddressesBottomSheet
import com.example.omnigo.features.customer.profile.presentation.ui.components.AccountMenuItem
import com.example.omnigo.features.customer.profile.presentation.ui.components.AccountQuickDashboard
import com.example.omnigo.features.customer.profile.presentation.ui.components.AvatarPickerBottomSheet
import com.example.omnigo.features.customer.profile.presentation.ui.components.ChangePasswordDialog
import com.example.omnigo.features.customer.profile.presentation.ui.components.CustomerHeroProfileCard
import com.example.omnigo.features.customer.profile.presentation.ui.components.LogoutConfirmationDialog
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountUiEvent
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountViewModel
import com.example.omnigo.features.language.presentation.ui.ChangeLanguageActivity
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
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

private val TOP_BAR_HEIGHT = 56.dp

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AccountScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    walletBalanceText: String? = null,
    onOpenWallet: () -> Unit = {},
    onOpenPromos: () -> Unit = {},
    onOpenActivity: () -> Unit = {},
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
                is AccountUiEvent.ShowToast -> context.showToast(event.message)
                is AccountUiEvent.ShowStringResToast -> context.showToast(event.resId)
            }
        }
    }

    if (uiState.showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = { viewModel.onConfirmLogout() },
            onDismiss = { viewModel.onDismissLogoutDialog() }
        )
    }

    OmniGoMainLayout(
        modifier = modifier,
        headerHeight = TOP_BAR_HEIGHT,
        header = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TOP_BAR_HEIGHT)
                    .padding(horizontal = Dimen.PaddingM),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(id = R.string.account_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimen.PaddingM)
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Customer Elevated Hero Profile Card
            CustomerHeroProfileCard(
                userName = uiState.userName,
                userPhone = uiState.userPhone,
                avatarUrl = uiState.avatarUrl,
                isUploadingAvatar = uiState.isUploadingAvatar,
                onProfileClick = {
                    context.startActivity(PersonalProfileActivity.createIntent(context))
                },
                onAvatarClick = { viewModel.onAvatarClick() }
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Quick Dashboard with Elevated Cards (OmniPay, Promo, Activity)
            AccountQuickDashboard(
                balance = walletBalanceText ?: stringResource(id = R.string.account_wallet_balance_mock),
                onWalletClick = onOpenWallet,
                onPromoClick = onOpenPromos,
                onActivityClick = onOpenActivity
            )

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Group 1: Account & Profile
            Text(
                text = stringResource(id = R.string.profile_section_account),
                style = MaterialTheme.typography.s12.semiBold(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimen.PaddingS, start = Dimen.PaddingXS)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppShape.ShapeL),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
                border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
            ) {
                Column {
                    AccountMenuItem(
                        icon = Icons.Outlined.PersonOutline,
                        title = stringResource(id = R.string.profile_personal_info_title),
                        onClick = {
                            context.startActivity(PersonalProfileActivity.createIntent(context))
                        }
                    )
                    HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.BorderHairline)
                    AccountMenuItem(
                        icon = Icons.Filled.Lock,
                        title = stringResource(id = R.string.account_change_password),
                        onClick = { viewModel.onOpenChangePassword() }
                    )
                    HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.BorderHairline)
                    AccountMenuItem(
                        icon = Icons.Filled.Bookmark,
                        title = stringResource(id = R.string.account_saved_places),
                        onClick = { viewModel.onOpenSavedAddresses() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Group 2: Settings & Support
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppShape.ShapeL),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
                border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
            ) {
                Column {
                    AccountMenuItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(id = R.string.language),
                        onClick = {
                            context.startActivity(Intent(context, ChangeLanguageActivity::class.java))
                        }
                    )
                    HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.BorderHairline)
                    AccountMenuItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = stringResource(id = R.string.account_help_center),
                        onClick = {
                            val gmailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@omnigo.vn")
                                setPackage("com.google.android.gm")
                            }
                            try {
                                context.startActivity(gmailIntent)
                            } catch (_: Exception) {
                                try {
                                    val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:support@omnigo.vn")
                                    }
                                    context.startActivity(fallbackIntent)
                                } catch (_: Exception) {
                                    // No email client available
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.BorderHairline)
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
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Bottom Navigation Bar Spacing to ensure full content scrolls clear of floating nav bar
            Spacer(modifier = Modifier.height(Dimen.HeightBottomNavPadding))
        }
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

    if (uiState.showChangePasswordDialog) {
        ChangePasswordDialog(
            isLoading = uiState.isChangingPassword,
            errorMessage = uiState.changePasswordError,
            onDismiss = { viewModel.onDismissChangePassword() },
            onSubmit = { oldPass, newPass, confirmPass ->
                viewModel.changePassword(oldPass, newPass, confirmPass)
            }
        )
    }

    if (uiState.showSavedAddressesSheet) {
        SavedAddressesBottomSheet(
            addresses = uiState.savedAddresses,
            isLoading = uiState.isLoadingSavedAddresses,
            onDismissRequest = { viewModel.onDismissSavedAddresses() },
            onSaveNewAddress = { label, addr -> viewModel.saveAddress(label, addr) },
            onUpdateAddress = { id, label, addr -> viewModel.updateAddress(id, label, addr) },
            onDeleteAddress = { id -> viewModel.deleteAddress(id) }
        )
    }
}
