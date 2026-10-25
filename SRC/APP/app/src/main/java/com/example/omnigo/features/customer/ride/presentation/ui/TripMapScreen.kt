package com.example.omnigo.features.customer.ride.presentation.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideLiveTrackingCard
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMapScreen(
    activeRideState: ActiveRideState?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (activeRideState != null && activeRideState.bookingId > 0) {
                            stringResource(id = R.string.trip_map_trip_id_format, activeRideState.bookingId)
                        } else {
                            stringResource(id = R.string.trip_map_title)
                        },
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight
                )
            )
        }
    ) { innerPadding ->
        if (activeRideState == null || activeRideState.isFinished) {
            TripMapEmptyState(
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            TripMapContent(
                activeRideState = activeRideState,
                onCallDriver = { phone ->
                    launchDialer(context, phone)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun TripMapContent(
    activeRideState: ActiveRideState,
    onCallDriver: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val targetLat = if (activeRideState.status == BookingStatus.IN_PROGRESS) {
        activeRideState.endLatitude
    } else {
        activeRideState.startLatitude
    }

    val targetLng = if (activeRideState.status == BookingStatus.IN_PROGRESS) {
        activeRideState.endLongitude
    } else {
        activeRideState.startLongitude
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingS)
    ) {
        // Live GPS Telemetry Card
        RideLiveTrackingCard(
            status = activeRideState.status,
            driverLocation = activeRideState.driverLiveLocation,
            targetLatitude = targetLat,
            targetLongitude = targetLng,
            distanceKm = activeRideState.liveDistanceKm,
            etaMinutes = activeRideState.liveEtaMinutes
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Driver Profile Card
        if (activeRideState.driverProfile != null) {
            TripMapDriverCard(
                activeRideState = activeRideState,
                onCallDriver = onCallDriver
            )
            Spacer(modifier = Modifier.height(AppSpacing.M))
        }

        // Route & Fare Card
        TripMapRouteCard(activeRideState = activeRideState)

        Spacer(modifier = Modifier.height(AppSpacing.XL))
    }
}

@Composable
private fun TripMapDriverCard(
    activeRideState: ActiveRideState,
    onCallDriver: (String) -> Unit
) {
    val driver = activeRideState.driverProfile ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = SurfaceLight
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXXL)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeL)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column(modifier = Modifier.weight(1f)) {
                val driverName = driver.driverName ?: stringResource(id = R.string.ride_driver_name_fallback)
                Text(
                    text = driverName,
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val model = driver.vehicleModel ?: ""
                val plate = driver.vehiclePlate ?: stringResource(id = R.string.ride_vehicle_plate_fallback)
                val vehicleDesc = if (model.isNotBlank()) "$model • $plate" else plate
                Text(
                    text = vehicleDesc,
                    style = MaterialTheme.typography.s14.medium(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val phone = driver.driverPhone
            if (!phone.isNullOrBlank()) {
                IconButton(
                    onClick = { onCallDriver(phone) },
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(PrimaryColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = stringResource(id = R.string.active_trip_sheet_call_driver),
                        tint = BackgroundLight,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
            }
        }
    }
}

@Composable
private fun TripMapRouteCard(activeRideState: ActiveRideState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Column {
                    Text(
                        text = stringResource(id = R.string.active_trip_sheet_pickup_label),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = activeRideState.pickupAddress.ifBlank { "—" },
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = ErrorColor,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Column {
                    Text(
                        text = stringResource(id = R.string.active_trip_sheet_dropoff_label),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = activeRideState.dropoffAddress.ifBlank { "—" },
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            val formatter = DecimalFormat("#,###")
            val priceStr = "${formatter.format(activeRideState.price)} đ"
            val paymentMethodText = if (activeRideState.paymentMethod == "CASH") {
                stringResource(id = R.string.active_trip_sheet_payment_cash)
            } else {
                stringResource(id = R.string.active_trip_sheet_payment_app)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(id = R.string.active_trip_sheet_fare_label)}: $priceStr",
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
                Text(
                    text = paymentMethodText,
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
            }
        }
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
        verticalArrangement = Arrangement.Center
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

private fun launchDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
