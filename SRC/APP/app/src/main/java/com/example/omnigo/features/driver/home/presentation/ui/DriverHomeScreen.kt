package com.example.omnigo.features.driver.home.presentation.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverApprovalWarningDialog
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverCockpitBottomPanel
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverCockpitMapBackground
import com.example.omnigo.features.driver.home.presentation.ui.components.DriverCockpitTopBar
import com.example.omnigo.features.driver.home.presentation.ui.components.FoodOrderOfferDialog
import com.example.omnigo.features.driver.home.presentation.ui.components.RideOfferDialog
import com.example.omnigo.features.driver.home.presentation.viewmodel.DriverHomeViewModel
import com.example.omnigo.features.driver.home.presentation.viewmodel.DriverRideOfferCoordinator
import com.example.omnigo.features.driver.service.DriverLocationForegroundService
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight

@Composable
fun DriverHomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToActiveDelivery: (Long, Double?, Double?) -> Unit = { _, _, _ -> },
    onNavigateToActiveTrip: (Long) -> Unit = {},
    viewModel: DriverHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.loadDailyReport()
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        DriverCockpitMapBackground(
            currentLatitude = uiState.currentLatitude,
            currentLongitude = uiState.currentLongitude,
            isOnline = uiState.isOnline,
            onRefreshLocation = { viewModel.refreshDriverLocation() },
            modifier = Modifier.fillMaxSize()
        )

        DriverCockpitTopBar(
            profile = uiState.profile,
            approvalStatus = uiState.approvalStatus,
            dailyReport = uiState.dailyReport,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
        )

        DriverCockpitBottomPanel(
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    start = Dimen.PaddingM,
                    end = Dimen.PaddingM,
                    bottom = Dimen.SizeMega
                )
        )
    }
}
