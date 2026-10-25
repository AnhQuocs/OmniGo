package com.example.omnigo.features.customer.home.presentation.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTripModalBottomSheet(
    activeRideState: ActiveRideState,
    onDismissRequest: () -> Unit,
    onOpenMapClick: () -> Unit,
    onCancelTripClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        containerColor = BackgroundLight,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL)
                .padding(bottom = Dimen.PaddingXL)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.active_trip_sheet_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Status Card
            ActiveTripStatusBanner(activeRideState = activeRideState)

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Driver & Vehicle Card (when assigned)
            if (activeRideState.driverProfile != null) {
                ActiveTripDriverCard(
                    activeRideState = activeRideState,
                    onCallDriverClick = {
                        val phone = activeRideState.driverProfile.driverPhone
                        if (!phone.isNullOrBlank()) {
                            launchDialer(context, phone)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(AppSpacing.M))
            }

            // Route Details Card
            ActiveTripRouteCard(activeRideState = activeRideState)

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Primary Action: Open Map
            Button(
                onClick = onOpenMapClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault),
                shape = RoundedCornerShape(AppShape.ShapeL),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Text(
                    text = stringResource(id = R.string.active_trip_sheet_btn_view_map),
                    style = MaterialTheme.typography.s16.semiBold()
                )
            }

            // Secondary Action: Cancel Trip (only if allowed)
            if (activeRideState.status == BookingStatus.PENDING || activeRideState.status == BookingStatus.ACCEPTED) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                OutlinedButton(
                    onClick = onCancelTripClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.ShapeL),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)
                ) {
                    Text(
                        text = stringResource(id = R.string.active_trip_sheet_cancel_trip),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = ErrorColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTripStatusBanner(activeRideState: ActiveRideState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = PrimaryColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeRideState.isSearchingDriver || activeRideState.status == BookingStatus.PENDING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeM),
                    color = PrimaryColor,
                    strokeWidth = Dimen.PaddingXXS
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeL)
                        .clip(CircleShape)
                        .background(PrimaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activeRideState.status == BookingStatus.IN_PROGRESS) {
                            Icons.Default.NearMe
                        } else {
                            Icons.Default.DirectionsCar
                        },
                        contentDescription = null,
                        tint = BackgroundLight,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column(modifier = Modifier.weight(1f)) {
                val statusText = when (activeRideState.status) {
                    BookingStatus.PENDING -> stringResource(id = R.string.active_trip_sheet_waiting_driver)
                    BookingStatus.ACCEPTED -> {
                        val eta = activeRideState.liveEtaMinutes ?: 5
                        val dist = activeRideState.liveDistanceKm ?: 1.5
                        stringResource(id = R.string.active_trip_banner_eta_format, eta, dist)
                    }
                    BookingStatus.ARRIVED -> stringResource(id = R.string.active_trip_banner_arrived)
                    BookingStatus.IN_PROGRESS -> stringResource(id = R.string.active_trip_banner_in_progress)
                    else -> stringResource(id = R.string.active_trip_sheet_title)
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.s14.bold(),
                    color = PrimaryColor
                )

                if (activeRideState.liveDistanceKm == null && activeRideState.status == BookingStatus.ACCEPTED) {
                    Text(
                        text = stringResource(id = R.string.active_trip_sheet_waiting_gps),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTripDriverCard(
    activeRideState: ActiveRideState,
    onCallDriverClick: () -> Unit
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

            IconButton(
                onClick = onCallDriverClick,
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

@Composable
private fun ActiveTripRouteCard(activeRideState: ActiveRideState) {
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
            // Pickup
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

            // Dropoff
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

            // Fare & Payment
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

private fun launchDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
