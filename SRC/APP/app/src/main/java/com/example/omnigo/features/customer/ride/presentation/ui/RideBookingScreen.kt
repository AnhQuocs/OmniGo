package com.example.omnigo.features.customer.ride.presentation.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import com.example.omnigo.features.customer.ride.presentation.ui.components.CustomerActiveTripContent
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideLocationPickerSection
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideMapCanvas
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideMapPinPicker
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
import com.example.omnigo.utils.s16
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

    BackHandler {
        if (uiState.isMapPinPicking) {
            viewModel.cancelMapPinPicking()
        } else {
            onBackClick()
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
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // 1. Interactive Center-Pin Map Mode
    if (uiState.isMapPinPicking) {
        val target = uiState.pinPickingTarget ?: LocationTarget.PICKUP
        val initialPoint = if (target == LocationTarget.PICKUP) uiState.pickupLocation else uiState.dropoffLocation
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

    // 3. Map-First Booking Canvas & Bottom Sheet Setup
    val formatter = DecimalFormat("#,###")
    val selectedPrice = uiState.pricingEstimate?.totalPrice

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 0: Map-First Canvas
        RideMapCanvas(
            pickupLocation = uiState.pickupLocation,
            dropoffLocation = uiState.dropoffLocation,
            nearbyDrivers = uiState.nearbyDrivers,
            selectedVehicleType = uiState.selectedVehicleType,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 1: Floating Transparent Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = Dimen.PaddingS,
                modifier = Modifier.size(Dimen.SizeXL)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = Dimen.PaddingS
            ) {
                Text(
                    text = stringResource(id = R.string.ride_booking_title),
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingS)
                )
            }
        }

        // Layer 2: Floating MyLocation FAB
        FloatingActionButton(
            onClick = viewModel::autoDetectPickupLocation,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimen.PaddingM, bottom = 390.dp)
                .shadow(Dimen.PaddingS, CircleShape)
        ) {
            if (uiState.isLocatingCurrentPosition) {
                CircularProgressIndicator(modifier = Modifier.size(Dimen.SizeS), strokeWidth = Dimen.PaddingXXS, color = PrimaryColor)
            } else {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
            }
        }

        // Layer 3: Draggable Bottom Control Sheet
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimen.PaddingM
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingM)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(OutlineLight)
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Location Picker
                RideLocationPickerSection(
                    pickupLocation = uiState.pickupLocation,
                    dropoffLocation = uiState.dropoffLocation,
                    isLocating = uiState.isLocatingCurrentPosition,
                    savedAddresses = uiState.savedAddresses,
                    onDetectCurrentLocation = viewModel::autoDetectPickupLocation,
                    onSelectPickup = viewModel::setPickupLocation,
                    onSelectDropoff = viewModel::setDropoffLocation,
                    onSwapClick = viewModel::swapLocations,
                    onOpenMapPicker = viewModel::startMapPinPicking
                )

                // Surge Banner if applicable
                val surge = uiState.pricingEstimate?.surgeMultiplier ?: 1.0
                if (surge > 1.0) {
                    Spacer(modifier = Modifier.height(AppSpacing.S))
                    RideSurgeBanner(surgeMultiplier = surge, surgeLevel = uiState.pricingEstimate?.surgeLevel)
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Vehicle Type Selector (BIKE, CAR_4, CAR_7)
                RideVehicleTypeSelector(
                    selectedType = uiState.selectedVehicleType,
                    vehicleEstimates = uiState.vehicleEstimates,
                    baselineEstimate = uiState.pricingEstimate,
                    isCalculating = uiState.isCalculatingPrice,
                    onSelectType = viewModel::selectVehicleType
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Book Now Button
                val isEnabled = uiState.pickupLocation != null &&
                        uiState.dropoffLocation != null &&
                        uiState.pricingEstimate != null &&
                        !uiState.isCreatingBooking &&
                        !uiState.isSearchingDriver &&
                        !uiState.isCalculatingPrice

                Button(
                    onClick = viewModel::createBooking,
                    enabled = isEnabled,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        disabledContainerColor = PrimaryColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    if (uiState.isCreatingBooking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.SizeM),
                            color = TextWhite,
                            strokeWidth = Dimen.PaddingXXS
                        )
                    } else {
                        val bookText = stringResource(id = R.string.ride_book_now_button)
                        val priceText = if (selectedPrice != null && selectedPrice > 0.0) {
                            " • ${formatter.format(selectedPrice)} đ"
                        } else ""
                        Text(
                            text = "$bookText$priceText",
                            color = TextWhite,
                            style = MaterialTheme.typography.s14.bold()
                        )
                    }
                }
            }
        }
    }

    // Radar & Timeout Dialogs
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
