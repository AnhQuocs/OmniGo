package com.example.omnigo.features.driver.profile.presentation.ui

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.profile.presentation.ui.PersonalProfileActivity
import com.example.omnigo.features.customer.profile.presentation.ui.components.AvatarPickerBottomSheet
import com.example.omnigo.features.customer.profile.presentation.ui.legal.TermsAndPrivacyActivity
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountUiEvent
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountViewModel
import com.example.omnigo.features.driver.profile.presentation.ui.components.DriverAccountTopBar
import com.example.omnigo.features.driver.profile.presentation.ui.components.DriverHeroProfileCard
import com.example.omnigo.features.driver.profile.presentation.ui.components.DriverMenuSection
import com.example.omnigo.features.driver.profile.presentation.ui.components.DriverVehicleInfoCard
import com.example.omnigo.features.language.presentation.ui.ChangeLanguageActivity
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAccountScreen(
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

    LaunchedEffect(viewModel) {
        viewModel.syncWithRemoteProfile()
        viewModel.loadDriverProfileInfo()
        viewModel.loadDriverBookingsCount()
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AccountUiEvent.NavigateToLogin -> onNavigateToLogin()
                is AccountUiEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AccountUiEvent.ShowStringResToast ->
                    Toast.makeText(context, context.getString(event.resId), Toast.LENGTH_SHORT).show()
            }
        }
    }

    OmniGoMainLayout(
        modifier = modifier,
        headerHeight = 56.dp,
        header = {
            DriverAccountTopBar(
                onNotificationClick = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.notifications_title),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSettingsClick = {
                    context.startActivity(PersonalProfileActivity.createIntent(context))
                },
                modifier = Modifier
                    .background(BackgroundLight)
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS)
            )
        }
    ) { contentModifier ->
        Column(
            modifier = contentModifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimen.PaddingM)
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.S))

            // 1. Hero Profile Card: Cloud Background + Avatar + Info + 3 Stats
            DriverHeroProfileCard(
                userName = uiState.userName,
                userPhone = uiState.userPhone,
                avatarUrl = uiState.avatarUrl,
                rating = uiState.rating,
                totalTrips = uiState.totalTrips,
                joinedMonths = uiState.joinedMonths,
                joinedDays = uiState.joinedDays,
                isUploadingAvatar = uiState.isUploadingAvatar,
                onAvatarClick = { viewModel.onAvatarClick() }
            )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // 3. Vehicle Information Card: OmniBike, Plate, Reg Date
        DriverVehicleInfoCard(
            vehiclePlate = uiState.vehiclePlate,
            approvalStatus = uiState.approvalStatus,
            registrationDate = uiState.registrationDate,
            onPlateClick = {
                context.startActivity(PersonalProfileActivity.createIntent(context))
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // 4. Menu Actions Section: Profile, Language, Terms, Help Center
        DriverMenuSection(
            onEditProfile = {
                context.startActivity(PersonalProfileActivity.createIntent(context))
            },
            onChangeLanguage = {
                context.startActivity(Intent(context, ChangeLanguageActivity::class.java))
            },
            onOpenTerms = {
                context.startActivity(TermsAndPrivacyActivity.createIntent(context))
            },
            onHelpCenter = {
                Toast.makeText(
                    context,
                    context.getString(R.string.driver_account_menu_support_title),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // 5. Logout Button (Red, Logout Icon + Text)
        AppButton(
            onClick = { viewModel.onLogoutClicked() },
            containerColor = ErrorColor,
            contentColor = OnPrimaryColor,
            isLoading = uiState.isLoading,
            shape = RoundedCornerShape(AppShape.ShapeL)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = OnPrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Text(
                    text = stringResource(id = R.string.account_logout),
                    style = MaterialTheme.typography.s16.bold(),
                    color = OnPrimaryColor
                )
            }
        }

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

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissLogoutDialog() },
            title = {
                Text(
                    text = stringResource(id = R.string.account_logout_confirm_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.account_logout_confirm_msg),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmLogout() }) {
                    Text(
                        text = stringResource(id = R.string.account_logout),
                        style = MaterialTheme.typography.s14.bold(),
                        color = ErrorColor
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissLogoutDialog() }) {
                    Text(
                        text = stringResource(id = R.string.account_logout_cancel),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                }
            },
            shape = RoundedCornerShape(AppShape.ShapeL)
        )
    }
}
