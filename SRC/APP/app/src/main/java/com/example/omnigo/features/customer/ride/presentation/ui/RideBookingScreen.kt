package com.example.omnigo.features.customer.ride.presentation.ui

import com.example.omnigo.utils.showToast
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.promos.presentation.ui.components.VoucherSelectionBottomSheet
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideActiveTripSheet
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideFloatingHeader
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideMapCanvas
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideMapPinPicker
import com.example.omnigo.features.customer.ride.presentation.ui.components.RidePlaceSearchOverlay
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideSearchingRadarDialog
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideSearchTimeoutDialog
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideTripReceiptDialog
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideVehicleSelectionSheet
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideBookingEvent
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideBookingViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import kotlinx.coroutines.launch

@Composable
fun RideBookingScreen(
    onBackClick: () -> Unit,
    initialPromoCode: String? = null,
    viewModel: RideBookingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(initialPromoCode) {
        if (!initialPromoCode.isNullOrBlank()) {
            viewModel.applyVoucherCode(initialPromoCode)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            if (isGpsEnabled) {
                viewModel.autoDetectPickupLocation()
            } else {
                context.showToast(R.string.ride_gps_required_msg)
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            context.showToast(R.string.ride_gps_required_msg)
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            if (isGpsEnabled) {
                viewModel.autoDetectPickupLocation()
            } else {
                context.showToast(R.string.ride_gps_required_msg)
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    BackHandler {
        when {
            uiState.isSearchingPlaces -> viewModel.closePlaceSearch()
            uiState.isMapPinPicking -> viewModel.cancelMapPinPicking()
            else -> onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is RideBookingEvent.ShowToast -> context.showToast(event.message)
                is RideBookingEvent.BookingCreated -> context.showToast(R.string.ride_booking_in_progress)
                is RideBookingEvent.BookingCancelled -> context.showToast(R.string.ride_booking_cancelled_success)
            }
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            context.showToast(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            context.showToast(it)
            viewModel.clearMessages()
        }
    }

    if (uiState.isMapPinPicking) {
        val target = uiState.pinPickingTarget ?: LocationTarget.PICKUP
        val initialPoint = uiState.pinPickedLocation
            ?: if (target == LocationTarget.PICKUP) uiState.pickupLocation else uiState.dropoffLocation
        RideMapPinPicker(
            target = target,
            initialPoint = initialPoint,
            onConfirmLocation = viewModel::confirmMapPinLocation,
            onBackClick = viewModel::cancelMapPinPicking,
            modifier = modifier
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        val isTripFinished = uiState.showTripCompletedReceipt || uiState.currentBooking?.status == BookingStatus.COMPLETED
        val activeRoute = when {
            isTripFinished -> emptyList()
            uiState.isTripActive -> uiState.activeTripRouteCoordinates.ifEmpty { uiState.routeCoordinates }
            else -> uiState.routeCoordinates
        }

        val coroutineScope = rememberCoroutineScope()
        val initialLat = uiState.pickupLocation?.latitude ?: 21.0285
        val initialLng = uiState.pickupLocation?.longitude ?: 105.8542
        val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
            position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(com.google.android.gms.maps.model.LatLng(initialLat, initialLng), 15f)
        }

        RideMapCanvas(
            pickupLocation = uiState.pickupLocation,
            dropoffLocation = uiState.dropoffLocation,
            nearbyDrivers = uiState.nearbyDrivers,
            selectedVehicleType = uiState.selectedVehicleType,
            routeCoordinates = activeRoute,
            driverLiveLocation = uiState.driverLiveLocation,
            tripStatus = uiState.currentBooking?.status,
            cameraPositionState = cameraPositionState,
            modifier = Modifier.fillMaxSize()
        )

        if (!uiState.isTripActive) {
            RideFloatingHeader(
                pickupLocation = uiState.pickupLocation,
                dropoffLocation = uiState.dropoffLocation,
                isLocatingPickup = uiState.isLocatingCurrentPosition,
                onPickupClick = { viewModel.openPlaceSearch(LocationTarget.PICKUP) },
                onDropoffClick = { viewModel.openPlaceSearch(LocationTarget.DROPOFF) },
                onSwapClick = viewModel::swapLocations,
                onBackClick = onBackClick,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        } else {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = Dimen.PaddingS,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = Dimen.PaddingM, top = Dimen.PaddingS)
                    .size(Dimen.SizeXLPlus)
                    .align(Alignment.TopStart)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                }
            }
        }

        val fabBottomPadding = if (uiState.isTripActive || uiState.isDualLocationReady) {
            Dimen.HeightRideBottomSheetOffset
        } else {
            Dimen.PaddingXL
        }

        FloatingActionButton(
            onClick = {
                if (uiState.isTripActive) {
                    coroutineScope.launch {
                        com.example.omnigo.features.customer.ride.presentation.ui.components.recenterMapCamera(
                            cameraPositionState, uiState.driverLiveLocation, uiState.pickupLocation, uiState.dropoffLocation,
                            uiState.currentBooking?.status == BookingStatus.IN_PROGRESS
                        )
                    }
                } else viewModel.autoDetectPickupLocation()
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimen.PaddingM, bottom = fabBottomPadding)
                .shadow(Dimen.PaddingS, CircleShape)
        ) {
            if (uiState.isLocatingCurrentPosition) {
                CircularProgressIndicator(modifier = Modifier.size(Dimen.SizeS), strokeWidth = Dimen.PaddingXXS, color = PrimaryColor)
            } else {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
            }
        }

        AnimatedVisibility(
            visible = !uiState.isTripActive && !isTripFinished && uiState.isDualLocationReady && !uiState.isSearchingDriver,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            RideVehicleSelectionSheet(
                selectedType = uiState.selectedVehicleType,
                estimates = uiState.vehicleEstimates,
                isCalculating = uiState.isCalculatingPrice,
                appliedVoucher = uiState.appliedVoucher,
                pendingPromoCode = uiState.pendingPromoCode,
                onSelectVehicle = viewModel::selectVehicleType,
                onOpenVoucherSheet = viewModel::openVoucherSheet,
                onConfirmBooking = viewModel::createBooking,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.isTripActive && uiState.currentBooking != null) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                RideActiveTripSheet(
                    booking = uiState.currentBooking!!,
                    driver = uiState.assignedDriver,
                    pickupLocation = uiState.pickupLocation,
                    dropoffLocation = uiState.dropoffLocation,
                    vehicleType = uiState.selectedVehicleType,
                    liveDistanceKm = uiState.liveDistanceKm,
                    liveEtaMinutes = uiState.liveEtaMinutes,
                    isCancelling = uiState.isCancellingBooking,
                    onCancelTrip = viewModel::cancelBooking,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isSearchingPlaces,
            enter = fadeIn() + slideInVertically { it / 6 },
            exit = fadeOut() + slideOutVertically { it / 6 },
            modifier = Modifier.fillMaxSize()
        ) {
            RidePlaceSearchOverlay(
                target = uiState.searchTarget,
                query = uiState.searchQuery,
                searchResults = uiState.searchResults,
                savedAddresses = uiState.savedAddresses,
                isLoading = uiState.isSearchingPlacesLoading,
                onQueryChange = viewModel::updateSearchQuery,
                onSelectPlace = viewModel::selectSearchResult,
                onPickOnMap = { target -> viewModel.startMapPinPicking(target) },
                onSelectCurrentLocation = {
                    viewModel.selectCurrentLocation(context.getString(R.string.ride_my_location))
                },
                onClose = viewModel::closePlaceSearch,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (uiState.isSearchingDriver && !uiState.showSearchTimeoutDialog) {
        RideSearchingRadarDialog(
            countdownSeconds = uiState.searchCountdownSeconds,
            isCancelling = uiState.isCancellingBooking,
            onCancelClick = viewModel::cancelBooking
        )
    }

    if (uiState.showSearchTimeoutDialog) {
        RideSearchTimeoutDialog(
            onRetry = viewModel::retrySearchDriver,
            onCancel = viewModel::dismissSearchTimeoutDialog
        )
    }

    if (uiState.showTripCompletedReceipt && uiState.completedBooking != null) {
        RideTripReceiptDialog(
            booking = uiState.completedBooking!!,
            onDismiss = viewModel::dismissCompletedTrip,
            onSubmitReview = viewModel::submitCompletedTripReview
        )
    }

    if (uiState.showVoucherSheet) {
        VoucherSelectionBottomSheet(
            vouchers = uiState.applicableVouchers,
            orderAmount = uiState.pricingEstimate?.totalPrice ?: 50000.0,
            appliedVoucherCode = uiState.appliedVoucher?.voucherCode,
            isLoading = uiState.isLoadingVouchers,
            onApplyCode = viewModel::applyVoucherCode,
            onRemoveVoucher = viewModel::removeAppliedVoucher,
            onDismiss = viewModel::closeVoucherSheet
        )
    }
}
