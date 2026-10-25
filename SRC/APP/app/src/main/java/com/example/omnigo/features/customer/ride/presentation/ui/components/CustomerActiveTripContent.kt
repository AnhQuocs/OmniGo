package com.example.omnigo.features.customer.ride.presentation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun CustomerActiveTripContent(
    uiState: RideBookingUiState,
    onCancelTrip: () -> Unit,
    onDismissCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val booking = uiState.currentBooking ?: return
    val driver = uiState.assignedDriver
    var showCancelDialog by remember { mutableStateOf(false) }

    val targetLat = if (booking.status == BookingStatus.IN_PROGRESS) booking.endLatitude else booking.startLatitude
    val targetLng = if (booking.status == BookingStatus.IN_PROGRESS) booking.endLongitude else booking.startLongitude

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        // 1. Trip Progress Status Banner
        TripProgressCard(booking = booking)

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // 2. Realtime Live GPS Tracking & Visual Route
        RideLiveTrackingCard(
            status = booking.status,
            driverLocation = uiState.driverLiveLocation,
            targetLatitude = targetLat,
            targetLongitude = targetLng,
            distanceKm = uiState.liveDistanceKm ?: booking.distanceInKm,
            etaMinutes = uiState.liveEtaMinutes
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // 3. Driver & Vehicle Information Card
        DriverVehicleCard(
            driver = driver,
            isLoading = uiState.isDriverProfileLoading,
            onCallDriver = { phone ->
                if (!phone.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                }
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // 4. Route & Fare Card
        ActiveTripRouteCard(booking = booking)

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // 5. Cancel Button (Only available when trip has not started)
        if (booking.status == BookingStatus.ACCEPTED || booking.status == BookingStatus.ARRIVED) {
            OutlinedButton(
                onClick = { showCancelDialog = true },
                enabled = !uiState.isCancellingBooking,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (uiState.isCancellingBooking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeSM),
                        color = ErrorColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.ride_cancel_active_trip_button),
                        style = MaterialTheme.typography.s14.semiBold()
                    )
                }
            }
        }
    }

    // Confirmation dialog before canceling
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
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
                    onClick = {
                        showCancelDialog = false
                        onCancelTrip()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text(
                        text = stringResource(id = R.string.ride_cancel_confirm_yes),
                        color = OnPrimaryColor
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.ride_cancel_confirm_no),
                        color = TextPrimary
                    )
                }
            }
        )
    }

    // Completed Trip Dialog
    if (booking.status == BookingStatus.COMPLETED) {
        AlertDialog(
            onDismissRequest = onDismissCompleted,
            title = {
                Text(
                    text = stringResource(id = R.string.ride_completed_dialog_title),
                    style = MaterialTheme.typography.s18.bold()
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(id = R.string.ride_completed_dialog_message),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.S))
                    val formattedFare = String.format(Locale.getDefault(), "%,.0f đ", booking.price)
                    Text(
                        text = "${stringResource(id = R.string.ride_fare_summary)}: $formattedFare",
                        style = MaterialTheme.typography.s16.bold(),
                        color = PrimaryColor
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissCompleted,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(
                        text = stringResource(id = R.string.ride_completed_done_button),
                        color = OnPrimaryColor
                    )
                }
            }
        )
    }
}
