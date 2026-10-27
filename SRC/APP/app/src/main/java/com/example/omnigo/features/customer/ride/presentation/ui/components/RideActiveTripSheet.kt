package com.example.omnigo.features.customer.ride.presentation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.text.DecimalFormat

private fun vehicleImageRes(type: VehicleType): Int = when (type) {
    VehicleType.CAR_4_SEAT -> R.drawable.omnicar4
    VehicleType.CAR_7_SEAT -> R.drawable.omnicar7
    VehicleType.BIKE, VehicleType.EXPRESS -> R.drawable.omnibike
}

@Composable
fun RideActiveTripSheet(
    booking: Booking,
    driver: BookingDriverProfile?,
    pickupLocation: LocationPoint?,
    dropoffLocation: LocationPoint?,
    vehicleType: VehicleType,
    liveDistanceKm: Double?,
    liveEtaMinutes: Int?,
    isCancelling: Boolean,
    onCancelTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimen.PaddingL
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(Dimen.SizeXL)
                    .height(Dimen.PaddingXS)
                    .clip(CircleShape)
                    .background(OutlineVariantLight)
            )
            Spacer(modifier = Modifier.height(AppSpacing.M))

            TripStatusHeader(
                status = booking.status,
                liveEtaMinutes = liveEtaMinutes,
                liveDistanceKm = liveDistanceKm
            )
            Spacer(modifier = Modifier.height(AppSpacing.M))

            TripRouteSummary(pickupLocation = pickupLocation, dropoffLocation = dropoffLocation)

            HorizontalDivider(
                color = OutlineVariantLight,
                thickness = Dimen.PaddingXXS,
                modifier = Modifier.padding(vertical = Dimen.PaddingSM)
            )

            TripDriverRow(driver = driver, vehicleType = vehicleType)

            HorizontalDivider(
                color = OutlineVariantLight,
                thickness = Dimen.PaddingXXS,
                modifier = Modifier.padding(vertical = Dimen.PaddingSM)
            )

            TripFareRow(price = booking.price)

            if (booking.status == BookingStatus.ACCEPTED || booking.status == BookingStatus.ARRIVED) {
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                TextButton(
                    onClick = { showCancelDialog = true },
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth().height(Dimen.HeightDefault)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.SizeSM),
                            color = ErrorColor,
                            strokeWidth = Dimen.PaddingXXS
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.ride_btn_cancel_trip_short),
                            style = MaterialTheme.typography.s14.semiBold(),
                            color = ErrorColor
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(AppSpacing.S))
            }
        }
    }

    if (showCancelDialog) {
        CancelTripDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                showCancelDialog = false
                onCancelTrip()
            }
        )
    }
}

@Composable
private fun TripStatusHeader(status: BookingStatus, liveEtaMinutes: Int?, liveDistanceKm: Double?) {
    val badgeTextRes = when (status) {
        BookingStatus.ACCEPTED -> R.string.ride_trip_enroute_badge
        BookingStatus.ARRIVED -> R.string.ride_trip_arrived_badge
        else -> R.string.ride_trip_in_progress_badge
    }
    val badgeColor = if (status == BookingStatus.IN_PROGRESS) SecondaryColor else SuccessColor
    val metrics = listOfNotNull(
        liveEtaMinutes?.let { stringResource(id = R.string.ride_trip_eta_minutes_format, it) },
        liveDistanceKm?.let { stringResource(id = R.string.ride_trip_distance_format, it) }
    ).joinToString(" • ")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = badgeTextRes),
            style = MaterialTheme.typography.s16.bold(),
            color = badgeColor,
            modifier = Modifier.weight(1f)
        )
        if (metrics.isNotBlank()) {
            Text(
                text = metrics,
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun TripRouteSummary(pickupLocation: LocationPoint?, dropoffLocation: LocationPoint?) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
        pickupLocation?.let { RoutePointRow(point = it, dotColor = SuccessColor) }
        dropoffLocation?.let { RoutePointRow(point = it, dotColor = SecondaryColor) }
    }
}

@Composable
private fun RoutePointRow(point: LocationPoint, dotColor: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(Dimen.PaddingSM)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(AppSpacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = point.name.ifBlank { point.address },
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (point.name.isNotBlank() && point.address.isNotBlank()) {
                Text(
                    text = point.address,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TripDriverRow(driver: BookingDriverProfile?, vehicleType: VehicleType) {
    val context = LocalContext.current
    val plate = driver?.vehiclePlate.orEmpty()
    val vehicleModel = driver?.vehicleModel ?: driver?.vehicleType.orEmpty()

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeXXL)
                .clip(CircleShape)
                .background(PrimaryColor.copy(alpha = 0.1f)),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = driver?.driverName ?: stringResource(id = R.string.driver_trip_title),
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(AppSpacing.XS))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
                Text(
                    text = stringResource(id = R.string.ride_trip_driver_rating_default),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.XXS))
            if (plate.isNotBlank()) {
                Surface(shape = RoundedCornerShape(AppShape.ShapeXXS), color = SurfaceVariantLight) {
                    Text(
                        text = plate,
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                    )
                }
            }
            if (vehicleModel.isNotBlank()) {
                Text(
                    text = vehicleModel,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Image(
            painter = painterResource(id = vehicleImageRes(vehicleType)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = Dimen.SizeXXLPlus, height = Dimen.SizeXXL)
        )
        Spacer(modifier = Modifier.width(AppSpacing.S))

        IconButton(
            onClick = {
                val phone = driver?.driverPhone
                if (!phone.isNullOrBlank()) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                }
            },
            modifier = Modifier
                .size(Dimen.SizeXLPlus)
                .clip(CircleShape)
                .background(SuccessColor)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = stringResource(id = R.string.ride_btn_call_driver),
                tint = OnPrimaryColor,
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}

@Composable
private fun TripFareRow(price: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeSM)
            )
            Spacer(modifier = Modifier.width(AppSpacing.S))
            Column {
                Text(
                    text = stringResource(id = R.string.ride_trip_fare_total_label),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextTertiary
                )
                Text(
                    text = stringResource(id = R.string.ride_payment_cash_label),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )
            }
        }
        Text(
            text = DecimalFormat("#,###").format(price) + " đ",
            style = MaterialTheme.typography.s18.bold(),
            color = PrimaryColor
        )
    }
}

@Composable
private fun CancelTripDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.ride_cancel_active_trip_dialog_title),
                style = MaterialTheme.typography.s18.bold()
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.ride_cancel_active_trip_dialog_message),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
            ) {
                Text(text = stringResource(id = R.string.ride_cancel_confirm_yes), color = OnPrimaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.ride_cancel_confirm_no), color = TextPrimary)
            }
        }
    )
}
