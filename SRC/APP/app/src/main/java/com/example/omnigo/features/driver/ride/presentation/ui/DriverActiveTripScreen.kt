package com.example.omnigo.features.driver.ride.presentation.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.util.LiveTrackingCalculator
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideLiveTrackingCard
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripActionSection
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripCustomerCard
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripHeaderBar
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripRouteCard
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripSettlementDialog
import com.example.omnigo.features.driver.ride.presentation.viewmodel.DriverActiveTripViewModel
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
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18

@Composable
fun DriverActiveTripScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverActiveTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler {
        if (uiState.isCompleted || uiState.isTripCancelled) {
            onNavigateBack()
        } else {
            viewModel.showCancelConfirmDialog(true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startPeriodicLocationTracking()
    }

    LaunchedEffect(uiState.isTripCancelled) {
        if (uiState.isTripCancelled) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (error != null) {
            val message = when (error) {
                DriverActiveTripViewModel.GEOFENCE_WARNING_PICKUP -> context.getString(R.string.driver_trip_geofence_warning_pickup)
                DriverActiveTripViewModel.GEOFENCE_WARNING_DROPOFF -> context.getString(R.string.driver_trip_geofence_warning_dropoff)
                else -> error
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            DriverTripHeaderBar(
                bookingId = uiState.bookingId,
                status = uiState.status,
                onNavigateBack = {
                    if (uiState.isCompleted || uiState.isTripCancelled) {
                        onNavigateBack()
                    } else {
                        viewModel.showCancelConfirmDialog(true)
                    }
                }
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(Dimen.PaddingL)
        ) {
            DriverTripCustomerCard(
                customerId = uiState.customerId,
                customerName = uiState.customerName,
                customerPhone = uiState.customerPhone,
                customerAvatarUrl = uiState.customerAvatarUrl
            )

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            val targetLat = if (uiState.isInProgress) uiState.endLatitude else uiState.startLatitude
            val targetLng = if (uiState.isInProgress) uiState.endLongitude else uiState.startLongitude
            val curLat = uiState.currentLatitude
            val curLng = uiState.currentLongitude
            val driverLiveLoc = if (curLat != null && curLng != null) {
                DriverLiveLocation(
                    latitude = curLat,
                    longitude = curLng
                )
            } else null

            RideLiveTrackingCard(
                status = uiState.status,
                driverLocation = driverLiveLoc,
                targetLatitude = targetLat,
                targetLongitude = targetLng,
                distanceKm = uiState.distanceToTargetMeters?.let { it / 1000.0 },
                etaMinutes = uiState.distanceToTargetMeters?.let {
                    LiveTrackingCalculator.calculateEtaMinutes(it / 1000.0, null)
                }
            )

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            DriverTripRouteCard(
                status = uiState.status,
                pickupAddress = uiState.pickupAddress,
                dropoffAddress = uiState.dropoffAddress,
                startLat = uiState.startLatitude,
                startLng = uiState.startLongitude,
                endLat = uiState.endLatitude,
                endLng = uiState.endLongitude,
                distanceToTargetMeters = uiState.distanceToTargetMeters,
                isWithinGeofence = uiState.isWithinGeofence
            )

            Spacer(modifier = Modifier.height(AppSpacing.XL))

            DriverTripActionSection(
                status = uiState.status,
                isWithinGeofence = uiState.isWithinGeofence,
                isUpdatingStatus = uiState.isUpdatingStatus,
                onArrivePickup = viewModel::onArrivePickup,
                onStartTrip = viewModel::onStartTrip,
                onCompleteTrip = viewModel::onCompleteTrip,
                onRequestCancel = { viewModel.showCancelConfirmDialog(true) }
            )
        }
    }

    // Cancellation confirmation dialog
    if (uiState.showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showCancelConfirmDialog(false) },
            shape = RoundedCornerShape(AppShape.ShapeXL),
            containerColor = SurfaceLight,
            title = {
                Text(
                    text = stringResource(R.string.driver_trip_cancel_dialog_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.driver_trip_cancel_dialog_message),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmCancelTrip,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = OnPrimaryColor
                    )
                ) {
                    Text(
                        text = stringResource(R.string.driver_trip_cancel_confirm),
                        style = MaterialTheme.typography.s14.bold()
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.showCancelConfirmDialog(false) },
                    shape = RoundedCornerShape(AppShape.ShapeM)
                ) {
                    Text(
                        text = stringResource(R.string.driver_trip_cancel_dismiss),
                        style = MaterialTheme.typography.s14.bold()
                    )
                }
            }
        )
    }

    // Settlement dialog
    if (uiState.showSettlementDialog) {
        DriverTripSettlementDialog(
            totalFare = uiState.price,
            platformFee = uiState.platformFee,
            driverEarnings = uiState.driverEarnings,
            isCashPayment = uiState.isCashPayment,
            onDismissAndFinish = {
                viewModel.dismissSettlementDialog()
                onNavigateBack()
            }
        )
    }
}
