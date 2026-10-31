package com.example.omnigo.features.driver.home.presentation.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverApprovalWarningDialog
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverHomeRadarMap
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverHomeTopCard
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverMapActionColumn
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverOnlineBottomCard
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverStatusPill
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverSupportBottomSheet
import com.example.omnigo.features.driver.home.presentation.ui.components.FoodOrderOfferDialog
import com.example.omnigo.features.driver.home.presentation.ui.components.RideOfferDialog
import com.example.omnigo.features.driver.home.presentation.viewmodel.DriverHomeViewModel
import com.example.omnigo.features.driver.home.presentation.viewmodel.DriverRideOfferCoordinator
import com.example.omnigo.features.driver.service.DriverLocationForegroundService
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun DriverHomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToActiveDelivery: (Long, Double?, Double?) -> Unit = { _, _, _ -> },
    onNavigateToActiveTrip: (Long) -> Unit = {},
    viewModel: DriverHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSupportSheet by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.loadDailyReport()
        viewModel.loadDriverStats()
        onPauseOrDispose { }
    }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.checkLocationPrerequisites()
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.refreshDriverLocation()
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

    LaunchedEffect(uiState.acceptedOrderId) {
        val orderId = uiState.acceptedOrderId ?: return@LaunchedEffect
        onNavigateToActiveDelivery(
            orderId,
            uiState.acceptedOfferRestaurantLat,
            uiState.acceptedOfferRestaurantLng
        )
        viewModel.clearAcceptedOrderId()
    }

    LaunchedEffect(uiState.acceptedRideBookingId) {
        val bookingId = uiState.acceptedRideBookingId ?: return@LaunchedEffect
        onNavigateToActiveTrip(bookingId)
        viewModel.clearAcceptedRideBookingId()
    }

    LaunchedEffect(uiState.offerActionMessage) {
        when (uiState.offerActionMessage) {
            DriverHomeViewModel.TIMEOUT_MESSAGE -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.driver_food_offer_timeout_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
            DriverRideOfferCoordinator.TIMEOUT_OFFER_MESSAGE -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.driver_ride_offer_timeout_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
            DriverRideOfferCoordinator.CANCELLED_OFFER_MESSAGE -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.driver_ride_offer_cancelled_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.clearOfferActionMessage()
    }

    if (showSupportSheet) {
        DriverSupportBottomSheet(
            onDismiss = { showSupportSheet = false }
        )
    }

    if (uiState.showApprovalWarningDialog) {
        DriverApprovalWarningDialog(
            approvalStatus = uiState.approvalStatus,
            onDismiss = { viewModel.dismissApprovalWarningDialog() }
        )
    }

    uiState.currentFoodOffer?.let { offer ->
        FoodOrderOfferDialog(
            offer = offer,
            countdownSeconds = uiState.offerCountdownSeconds,
            countdownProgress = uiState.offerCountdownProgress,
            isAccepting = uiState.isOfferAccepting,
            isRejecting = uiState.isOfferRejecting,
            onAccept = { viewModel.onAcceptOffer() },
            onReject = { viewModel.onRejectOffer() }
        )
    }

    uiState.currentRideOffer?.let { offer ->
        RideOfferDialog(
            offer = offer,
            countdownSeconds = uiState.rideOfferCountdownSeconds,
            countdownProgress = uiState.rideOfferCountdownProgress,
            isAccepting = uiState.isRideOfferAccepting,
            isRejecting = uiState.isRideOfferRejecting,
            onAccept = { viewModel.onAcceptRideOffer() },
            onReject = { viewModel.onRejectRideOffer() }
        )
    }

    val targetLat = uiState.currentLatitude ?: 10.776889
    val targetLng = uiState.currentLongitude ?: 106.700806
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(targetLat, targetLng), 16.5f)
    }
    val coroutineScope = rememberCoroutineScope()

    val currentBearing = cameraPositionState.position.bearing
    val isCompassRotated = kotlin.math.abs(currentBearing) > 1.0f
    val isGpsCentered = remember(cameraPositionState.position.target, targetLat, targetLng) {
        val dLat = kotlin.math.abs(cameraPositionState.position.target.latitude - targetLat)
        val dLng = kotlin.math.abs(cameraPositionState.position.target.longitude - targetLng)
        dLat < 0.0005 && dLng < 0.0005
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // 1. Interactive Google Map with Radar Pulse
        DriverHomeRadarMap(
            currentLatitude = uiState.currentLatitude,
            currentLongitude = uiState.currentLongitude,
            isOnline = uiState.isOnline,
            cameraPositionState = cameraPositionState,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Floating Hero Profile & Today's Earnings Card
        DriverHomeTopCard(
            profile = uiState.profile,
            approvalStatus = uiState.approvalStatus,
            dailyReport = uiState.dailyReport,
            rating = uiState.rating,
            totalTrips = uiState.totalTrips,
            joinedMonths = uiState.joinedMonths,
            joinedDays = uiState.joinedDays,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS)
        )

        // 3. Floating Right Map Action Column (🎧 Support, 🧭 Compass, 📍 Recenter)
        DriverMapActionColumn(
            isSupportActive = showSupportSheet,
            isCompassActive = isCompassRotated,
            isGpsCentered = isGpsCentered,
            bearing = currentBearing,
            onSupportClick = {
                showSupportSheet = true
            },
            onCompassClick = {
                if (isCompassRotated) {
                    coroutineScope.launch {
                        runCatching {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder(cameraPositionState.position)
                                        .bearing(0f)
                                        .tilt(0f)
                                        .build()
                                )
                            )
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.driver_map_compass_aligned),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onRecenterClick = {
                viewModel.refreshDriverLocation()
                coroutineScope.launch {
                    val lat = uiState.currentLatitude ?: 10.776889
                    val lng = uiState.currentLongitude ?: 106.700806
                    runCatching {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16.5f)
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = Dimen.PaddingM)
        )

        // 4. Bottom Controls: Status Pill + Online/Offline Card (No Switch)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    start = Dimen.PaddingM,
                    end = Dimen.PaddingM,
                    bottom = Dimen.SizeMega
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            DriverStatusPill(
                isOnline = uiState.isOnline
            )

            DriverOnlineBottomCard(
                isOnline = uiState.isOnline,
                isUpdating = uiState.isStatusUpdating,
                activeRideBooking = uiState.activeRideBooking,
                activeFoodOrder = uiState.activeFoodOrder,
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
                },
                onResumeRide = { booking ->
                    onNavigateToActiveTrip(booking.bookingId)
                },
                onResumeFoodOrder = { order ->
                    onNavigateToActiveDelivery(order.orderId, null, null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
