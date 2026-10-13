package com.example.omnigo.features.driver.home.presentation.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverApprovalWarningDialog
import com.example.omnigo.features.driver.home.presentation.viewmodel.DriverHomeViewModel
import com.example.omnigo.features.driver.service.DriverLocationForegroundService
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OnSurfaceVariantLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold

@Composable
fun DriverHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DriverHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.checkLocationPrerequisites()
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.toggleOnlineStatus(true)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.driver_permission_location_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(uiState.isOnline) {
        if (uiState.isOnline) {
            DriverLocationForegroundService.startTracking(context)
        } else {
            DriverLocationForegroundService.stopTracking(context)
        }
    }

    val locReqMsg = stringResource(id = R.string.driver_permission_location_required)
    val gpsReqMsg = stringResource(id = R.string.driver_gps_enable_required)

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage ?: return@LaunchedEffect
        when (error) {
            DriverHomeViewModel.ERROR_LOCATION_PERMISSION_REQUIRED -> {
                val perms = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(perms.toTypedArray())
            }
            DriverHomeViewModel.ERROR_GPS_REQUIRED -> {
                Toast.makeText(context, gpsReqMsg, Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.clearErrorMessage()
    }

    if (uiState.showApprovalWarningDialog) {
        DriverApprovalWarningDialog(
            approvalStatus = uiState.approvalStatus,
            onDismiss = { viewModel.dismissApprovalWarningDialog() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        DriverStatusCard(
            isOnline = uiState.isOnline,
            isUpdating = uiState.isStatusUpdating,
            onToggleStatus = { targetOnline ->
                if (targetOnline && !uiState.isLocationPermissionGranted) {
                    val perms = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(perms.toTypedArray())
                } else {
                    viewModel.toggleOnlineStatus(targetOnline)
                }
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        DriverTodaySummaryCard()

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        DriverRadarCard(isOnline = uiState.isOnline)

        Spacer(modifier = Modifier.height(AppSpacing.XXL))
    }
}

@Composable
private fun DriverStatusCard(
    isOnline: Boolean,
    isUpdating: Boolean,
    onToggleStatus: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBgColor by animateColorAsState(
        targetValue = if (isOnline) SuccessColor.copy(alpha = 0.12f) else SurfaceLight,
        animationSpec = tween(durationMillis = 300),
        label = "driverStatusBg"
    )

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = statusBgColor),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = if (isOnline) SuccessColor.copy(alpha = 0.4f) else CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.PaddingM)
                        .clip(CircleShape)
                        .background(if (isOnline) SuccessColor else OnSurfaceVariantLight)
                )

                Column {
                    Text(
                        text = stringResource(
                            id = if (isOnline) R.string.driver_status_online else R.string.driver_status_offline
                        ),
                        style = MaterialTheme.typography.s18.bold(),
                        color = if (isOnline) SuccessColor else TextPrimary
                    )
                    Text(
                        text = stringResource(
                            id = if (isOnline) R.string.driver_status_online_hint else R.string.driver_status_offline_hint
                        ),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }

            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeL),
                    color = PrimaryColor,
                    strokeWidth = Dimen.PaddingXXS
                )
            } else {
                Switch(
                    checked = isOnline,
                    onCheckedChange = onToggleStatus,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnPrimaryColor,
                        checkedTrackColor = SuccessColor,
                        uncheckedThumbColor = SurfaceLight,
                        uncheckedTrackColor = OutlineVariantLight
                    )
                )
            }
        }
    }
}

@Composable
private fun DriverTodaySummaryCard(
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
                text = stringResource(id = R.string.driver_today_earnings),
                style = MaterialTheme.typography.s13.normal(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            Text(
                text = "450.000 đ",
                style = MaterialTheme.typography.s20.bold(),
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DriverStatColumn(
                    title = stringResource(id = R.string.driver_completed_trips),
                    value = "8"
                )
                DriverStatColumn(
                    title = stringResource(id = R.string.driver_acceptance_rate),
                    value = "98%"
                )
                DriverStatColumn(
                    title = stringResource(id = R.string.driver_rating),
                    value = "⭐ 4.9"
                )
            }
        }
    }
}

@Composable
private fun DriverStatColumn(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.s12.normal(),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(AppSpacing.XXS))
        Text(
            text = value,
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )
    }
}

@Composable
private fun DriverRadarCard(
    isOnline: Boolean,
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
                .padding(Dimen.PaddingL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeUltra)
                    .clip(CircleShape)
                    .background(if (isOnline) PrimaryContainer else OutlineVariantLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_activity),
                    contentDescription = null,
                    tint = if (isOnline) PrimaryColor else OnSurfaceVariantLight,
                    modifier = Modifier.size(Dimen.SizeXL)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            Text(
                text = if (isOnline) {
                    stringResource(id = R.string.driver_scanning_orders)
                } else {
                    stringResource(id = R.string.driver_status_offline_hint)
                },
                style = MaterialTheme.typography.s14.medium(),
                color = if (isOnline) PrimaryColor else TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
