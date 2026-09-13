package com.example.omnigo.features.driver.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountUiEvent
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DriverAccountScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AccountUiEvent.NavigateToLogin -> onNavigateToLogin()
                is AccountUiEvent.ShowToast -> {}
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        Text(
            text = stringResource(id = R.string.driver_nav_account),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // Driver Profile Header Card
        DriverProfileHeaderCard(
            userName = uiState.userName,
            userPhone = uiState.userPhone
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Vehicle Information Card
        DriverVehicleInfoCard()

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // Logout Button
        AppButton(
            text = stringResource(id = R.string.account_logout),
            containerColor = ErrorColor,
            contentColor = OnPrimaryColor,
            isLoading = uiState.isLoading,
            onClick = { viewModel.onLogoutClicked() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.XXL))
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
                TextButton(
                    onClick = { viewModel.onConfirmLogout() }
                ) {
                    Text(
                        text = stringResource(id = R.string.account_logout),
                        style = MaterialTheme.typography.s14.bold(),
                        color = ErrorColor
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onDismissLogoutDialog() }
                ) {
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

@Composable
private fun DriverProfileHeaderCard(
    userName: String,
    userPhone: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXXLPlus)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    style = MaterialTheme.typography.s20.bold(),
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = userPhone,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXS),
                    color = PrimaryContainer
                ) {
                    Text(
                        text = "⭐ 4.9 • Đối tác tài xế",
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = PrimaryColor,
                        modifier = Modifier.padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverVehicleInfoCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.driver_vehicle_info),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_vehicle_type),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = "Xe máy điện / Motorbike",
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = TextPrimary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.PaddingS),
                color = OutlineVariantLight
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_license_plate),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = "29A-123.45",
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = PrimaryColor
                )
            }
        }
    }
}
