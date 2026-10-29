package com.example.omnigo.features.driver.profile.presentation.ui

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.features.customer.profile.presentation.ui.PersonalProfileActivity
import com.example.omnigo.features.customer.profile.presentation.ui.components.AvatarPickerBottomSheet
import com.example.omnigo.features.customer.profile.presentation.ui.legal.TermsAndPrivacyActivity
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountUiEvent
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountViewModel
import com.example.omnigo.features.driver.profile.presentation.ui.components.DriverProfileHeaderCard
import com.example.omnigo.features.driver.profile.presentation.ui.components.DriverSettingsCard
import com.example.omnigo.features.driver.profile.presentation.ui.components.DriverVehicleInfoCard
import com.example.omnigo.features.language.presentation.ui.ChangeLanguageActivity
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
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
        viewModel.loadDriverProfileInfo()
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingM)
    ) {
        Text(
            text = stringResource(id = R.string.driver_nav_account),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        DriverProfileHeaderCard(
            userName = uiState.userName,
            userPhone = uiState.userPhone,
            avatarUrl = uiState.avatarUrl,
            approvalStatus = uiState.approvalStatus,
            isUploadingAvatar = uiState.isUploadingAvatar,
            onAvatarClick = { viewModel.onAvatarClick() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DriverVehicleInfoCard(
            vehiclePlate = uiState.vehiclePlate,
            approvalStatus = uiState.approvalStatus
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DriverSettingsCard(
            onEditProfile = {
                context.startActivity(PersonalProfileActivity.createIntent(context))
            },
            onChangeLanguage = {
                context.startActivity(Intent(context, ChangeLanguageActivity::class.java))
            },
            onOpenTerms = {
                context.startActivity(TermsAndPrivacyActivity.createIntent(context))
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        AppButton(
            text = stringResource(id = R.string.account_logout),
            containerColor = ErrorColor,
            contentColor = OnPrimaryColor,
            isLoading = uiState.isLoading,
            onClick = { viewModel.onLogoutClicked() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.XXLPlus))
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
                        text = stringResource(id = R.string.cancel),
                        style = MaterialTheme.typography.s14.medium(),
                        color = TextSecondary
                    )
                }
            },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(AppShape.ShapeL)
        )
    }
}
