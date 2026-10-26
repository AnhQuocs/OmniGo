package com.example.omnigo.features.customer.ride.presentation.ui

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import com.example.omnigo.features.customer.ride.presentation.ui.components.CustomerActiveTripContent
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideFloatingHeader
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideMapCanvas
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideMapPinPicker
import com.example.omnigo.features.customer.ride.presentation.ui.components.RidePlaceSearchOverlay
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideSearchingRadarDialog
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideSurgeBanner
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideVehicleTypeSelector
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideBookingEvent
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideBookingViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideBookingScreen(
    onBackClick: () -> Unit,
    viewModel: RideBookingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 0. Mandatory Location & GPS Enforcement
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
                Toast.makeText(context, context.getString(R.string.ride_gps_required_msg), Toast.LENGTH_LONG).show()
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            Toast.makeText(context, context.getString(R.string.ride_gps_required_msg), Toast.LENGTH_LONG).show()
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
                Toast.makeText(context, context.getString(R.string.ride_gps_required_msg), Toast.LENGTH_LONG).show()
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
                is RideBookingEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is RideBookingEvent.BookingCreated -> Toast.makeText(context, context.getString(R.string.ride_booking_in_progress), Toast.LENGTH_SHORT).show()
                is RideBookingEvent.BookingCancelled -> Toast.makeText(context, context.getString(R.string.ride_booking_cancelled_success), Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // 1. Interactive Center-Pin Map Mode
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

    // 2. Active Trip Progress View
    if (uiState.isTripActive) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.ride_active_trip_title),
                            style = MaterialTheme.typography.s18.bold(),
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { innerPadding ->
            CustomerActiveTripContent(
                uiState = uiState,
                onCancelTrip = viewModel::cancelBooking,
                onDismissCompleted = viewModel::dismissCompletedTrip,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            )
        }
        return
    }

    // 3. Map-First Booking Canvas & Controls
    Box(modifier = modifier.fillMaxSize()) {
        // Layer 0: Map-First Canvas
        RideMapCanvas(
            pickupLocation = uiState.pickupLocation,
            dropoffLocation = uiState.dropoffLocation,
            nearbyDrivers = uiState.nearbyDrivers,
            selectedVehicleType = uiState.selectedVehicleType,
            routeCoordinates = uiState.routeCoordinates,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 1: Floating Trip Header (Pickup & Dropoff)
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

        // Layer 2: Floating MyLocation FAB
        val fabBottomPadding = if (uiState.isDualLocationReady) {
            Dimen.HeightRideBottomSheetOffset
        } else {
            Dimen.PaddingXL
        }

        FloatingActionButton(
            onClick = viewModel::autoDetectPickupLocation,
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

        // Layer 3: Minimal & Clean Bottom Sheet (Only visible when pickup & dropoff are selected)
        AnimatedVisibility(
            visible = uiState.isDualLocationReady,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            com.example.omnigo.features.customer.ride.presentation.ui.components.RideBookingBottomSheet(
                uiState = uiState,
                onSelectVehicleType = viewModel::selectVehicleType,
                onCreateBooking = viewModel::createBooking
            )
        }

        // Layer 4: Autocomplete Place Search Fullscreen Overlay (no map teardown, no black flicker)
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

    // 5. Radar & Timeout Dialogs
    if (uiState.isSearchingDriver && !uiState.showSearchTimeoutDialog) {
        RideSearchingRadarDialog(
            countdownSeconds = uiState.searchCountdownSeconds,
            isCancelling = uiState.isCancellingBooking,
            onCancelClick = viewModel::cancelBooking
        )
    }

    if (uiState.showSearchTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSearchTimeoutDialog() },
            title = {
                Text(
                    text = stringResource(id = R.string.ride_search_timeout_dialog_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.ride_search_timeout_dialog_message),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.retrySearchDriver() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(AppShape.ShapeM)
                ) {
                    Text(
                        text = stringResource(id = R.string.ride_search_timeout_btn_retry),
                        style = MaterialTheme.typography.s14.semiBold()
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissSearchTimeoutDialog() },
                    shape = RoundedCornerShape(AppShape.ShapeM)
                ) {
                    Text(
                        text = stringResource(id = R.string.ride_search_timeout_btn_cancel),
                        style = MaterialTheme.typography.s14.normal()
                    )
                }
            }
        )
    }
}
