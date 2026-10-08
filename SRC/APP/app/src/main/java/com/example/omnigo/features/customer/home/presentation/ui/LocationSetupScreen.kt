package com.example.omnigo.features.customer.home.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.home.presentation.ui.components.AddressPickerBottomSheet
import com.example.omnigo.features.customer.home.presentation.viewmodel.LocationSetupUiEvent
import com.example.omnigo.features.customer.home.presentation.viewmodel.LocationSetupViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s24

@Composable
fun LocationSetupScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationSetupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun checkPermissionAndGps(): Pair<Boolean, Boolean> {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsOn = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        return Pair(hasPermission, isGpsOn)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val (perm, gps) = checkPermissionAndGps()
                viewModel.updateHardwareAndPermissionState(perm, gps)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val (perm, gps) = checkPermissionAndGps()
        viewModel.updateHardwareAndPermissionState(perm, gps)

        viewModel.uiEvent.collect { event ->
            when (event) {
                is LocationSetupUiEvent.NavigateToHome -> onNavigateToHome()
                is LocationSetupUiEvent.OpenLocationSettings -> {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }
                is LocationSetupUiEvent.RequestLocationPermission -> {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = BackgroundLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimen.PaddingL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.XXL))

            // Center Illustration & Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulseScale)
                            .alpha(pulseAlpha)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    )

                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                            .border(2.dp, PrimaryColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeXLPlus)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.XL))

                Text(
                    text = stringResource(id = R.string.location_setup_title),
                    style = MaterialTheme.typography.s24.bold(),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                Text(
                    text = stringResource(id = R.string.location_setup_subtitle),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dimen.PaddingS)
                )
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimen.PaddingXL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonTextRes = if (!uiState.hasPermission) {
                    R.string.location_setup_btn_permission
                } else {
                    R.string.location_setup_btn_gps
                }

                val buttonIcon = if (!uiState.hasPermission) {
                    Icons.Filled.LockOpen
                } else {
                    Icons.Filled.MyLocation
                }

                Button(
                    onClick = {
                        val (hasPermission, isGpsOn) = checkPermissionAndGps()
                        viewModel.onPrimaryButtonClicked(hasPermission, isGpsOn)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.ShapeXL2),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    enabled = !uiState.isDetectingGps
                ) {
                    if (uiState.isDetectingGps) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.SizeM),
                            color = TextWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = R.string.home_location_locating),
                            style = MaterialTheme.typography.s16.bold(),
                            color = TextWhite
                        )
                    } else {
                        Icon(
                            imageVector = buttonIcon,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = buttonTextRes),
                            style = MaterialTheme.typography.s16.bold(),
                            color = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                OutlinedButton(
                    onClick = viewModel::onOpenManualPickerClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.ShapeXL2),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditLocationAlt,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = stringResource(id = R.string.location_setup_btn_manual),
                        style = MaterialTheme.typography.s15.medium(),
                        color = TextPrimary
                    )
                }
            }
        }
    }

    if (uiState.showAddressPickerSheet) {
        AddressPickerBottomSheet(
            provinces = uiState.provinces,
            districts = uiState.districts,
            wards = uiState.wards,
            selectedProvince = uiState.selectedProvince,
            selectedDistrict = uiState.selectedDistrict,
            selectedWard = uiState.selectedWard,
            streetDetail = uiState.streetDetail,
            currentStep = uiState.addressPickerStep,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onProvinceSelect = viewModel::onSelectProvince,
            onDistrictSelect = viewModel::onSelectDistrict,
            onWardSelect = viewModel::onSelectWard,
            onStreetDetailChange = viewModel::onStreetDetailChange,
            onStepChange = viewModel::onStepChange,
            onConfirmAddress = viewModel::onConfirmCustomAddress,
            onDismissRequest = viewModel::dismissAddressPicker
        )
    }
}
