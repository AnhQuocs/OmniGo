package com.example.omnigo.features.customer.ride.presentation.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideActiveTripSheet
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideMapCanvas
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun TripMapScreen(
    activeRideState: ActiveRideState?,
    onBackClick: () -> Unit,
    onCancelTrip: () -> Unit = {},
    isCancelling: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (activeRideState == null || activeRideState.isFinished) {
        TripMapEmptyState(
            onBackClick = onBackClick,
            modifier = modifier.fillMaxSize()
        )
    } else {
        TripMapContent(
            activeRideState = activeRideState,
            onBackClick = onBackClick,
            onCancelTrip = onCancelTrip,
            isCancelling = isCancelling,
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TripMapContent(
    activeRideState: ActiveRideState,
    onBackClick: () -> Unit,
    onCancelTrip: () -> Unit,
    isCancelling: Boolean,
    modifier: Modifier = Modifier
) {
    val pickupLocation = remember(
        activeRideState.startLatitude,
        activeRideState.startLongitude,
        activeRideState.pickupAddress
    ) {
        if (activeRideState.startLatitude != 0.0 && activeRideState.startLongitude != 0.0) {
            LocationPoint(
                name = activeRideState.pickupAddress.ifBlank { "Điểm đón" },
                address = activeRideState.pickupAddress,
                latitude = activeRideState.startLatitude,
                longitude = activeRideState.startLongitude
            )
        } else null
    }

    val dropoffLocation = remember(
        activeRideState.endLatitude,
        activeRideState.endLongitude,
        activeRideState.dropoffAddress
    ) {
        if (activeRideState.endLatitude != 0.0 && activeRideState.endLongitude != 0.0) {
            LocationPoint(
                name = activeRideState.dropoffAddress.ifBlank { "Điểm đến" },
                address = activeRideState.dropoffAddress,
                latitude = activeRideState.endLatitude,
                longitude = activeRideState.endLongitude
            )
        } else null
    }

    val vehicleType = remember(activeRideState.driverProfile?.vehicleType) {
        when (activeRideState.driverProfile?.vehicleType?.uppercase()) {
            "CAR_4_SEAT" -> VehicleType.CAR_4_SEAT
            "CAR_7_SEAT" -> VehicleType.CAR_7_SEAT
            else -> VehicleType.BIKE
        }
    }

    val booking = remember(activeRideState) {
        Booking(
            bookingId = activeRideState.bookingId,
            status = activeRideState.status,
            price = activeRideState.price,
            paymentMethod = activeRideState.paymentMethod,
            startLatitude = activeRideState.startLatitude,
            startLongitude = activeRideState.startLongitude,
            endLatitude = activeRideState.endLatitude,
            endLongitude = activeRideState.endLongitude,
            pickupAddress = activeRideState.pickupAddress,
            dropoffAddress = activeRideState.dropoffAddress
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        val initialLat = pickupLocation?.latitude ?: 10.776889
        val initialLng = pickupLocation?.longitude ?: 106.700806
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 15f)
        }
        val coroutineScope = rememberCoroutineScope()

        // Layer 0: Full Screen Realtime Map
        RideMapCanvas(
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            nearbyDrivers = emptyList(),
            selectedVehicleType = vehicleType,
            routeCoordinates = activeRideState.routeCoordinates,
            driverLiveLocation = activeRideState.driverLiveLocation,
            tripStatus = activeRideState.status,
            cameraPositionState = cameraPositionState,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 1: Floating Header Bar (Back button + Trip badge)
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
                modifier = Modifier.size(Dimen.SizeXLPlus)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.S))

            Surface(
                shape = RoundedCornerShape(AppShape.PillShape),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = Dimen.PaddingS
            ) {
                Text(
                    text = if (activeRideState.bookingId > 0) {
                        stringResource(id = R.string.trip_map_trip_id_format, activeRideState.bookingId)
                    } else {
                        stringResource(id = R.string.trip_map_title)
                    },
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
                )
            }
        }

        // Layer 2: Floating Re-center FAB
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    com.example.omnigo.features.customer.ride.presentation.ui.components.recenterMapCamera(
                        cameraPositionState = cameraPositionState,
                        driverLocation = activeRideState.driverLiveLocation,
                        pickupLocation = pickupLocation,
                        dropoffLocation = dropoffLocation,
                        isInProgress = activeRideState.status == com.example.omnigo.features.customer.ride.domain.model.BookingStatus.IN_PROGRESS
                    )
                }
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimen.PaddingM, bottom = Dimen.HeightRideBottomSheetOffset)
                .shadow(Dimen.PaddingS, CircleShape)
        ) {
            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
        }

        // Layer 3: In-Trip Bottom Tracking Sheet
        RideActiveTripSheet(
            booking = booking,
            driver = activeRideState.driverProfile,
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            vehicleType = vehicleType,
            liveDistanceKm = activeRideState.liveDistanceKm,
            liveEtaMinutes = activeRideState.liveEtaMinutes,
            isCancelling = isCancelling,
            onCancelTrip = onCancelTrip,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun TripMapEmptyState(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimen.PaddingXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.trip_map_no_active_trip),
            style = MaterialTheme.typography.s16.medium(),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(AppSpacing.L))
        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(AppShape.ShapeL),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text(
                text = stringResource(id = R.string.trip_map_close_button),
                style = MaterialTheme.typography.s14.semiBold()
            )
        }
    }
}
