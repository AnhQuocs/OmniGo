package com.example.omnigo.features.customer.ride.presentation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        // 1. Trip Progress Status Card with Smooth Animated Banner
        TripProgressCard(booking = booking)

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // 2. Driver & Vehicle Information Card
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

        // 3. Route & Fare Card
        ActiveTripRouteCard(booking = booking)

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // 4. Cancel Button (Only available if trip has not started)
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

@Composable
private fun TripProgressCard(booking: Booking) {
    val statusText = when (booking.status) {
        BookingStatus.ACCEPTED -> stringResource(id = R.string.ride_stepper_accepted_title)
        BookingStatus.ARRIVED -> stringResource(id = R.string.ride_stepper_arrived_title)
        BookingStatus.IN_PROGRESS -> stringResource(id = R.string.ride_stepper_in_progress_title)
        BookingStatus.COMPLETED -> stringResource(id = R.string.ride_stepper_completed_title)
        else -> stringResource(id = R.string.ride_active_trip_title)
    }

    val hintText = when (booking.status) {
        BookingStatus.ACCEPTED -> stringResource(id = R.string.ride_status_accepted_hint)
        BookingStatus.ARRIVED -> stringResource(id = R.string.ride_status_arrived_hint)
        BookingStatus.IN_PROGRESS -> stringResource(id = R.string.ride_status_in_progress_hint)
        else -> ""
    }

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = PrimaryColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingL)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .clip(CircleShape)
                        .background(SuccessColor)
                )
                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        (fadeIn() + slideInVertically()).togetherWith(fadeOut() + slideOutVertically())
                    },
                    label = "tripStatusTitleAnimation"
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                }
            }

            if (hintText.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DriverVehicleCard(
    driver: BookingDriverProfile?,
    isLoading: Boolean,
    onCallDriver: (String?) -> Unit
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        if (isLoading && driver == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeL),
                    color = PrimaryColor,
                    strokeWidth = Dimen.PaddingXXS
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.M),
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeMega)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                            .border(Dimen.PaddingXXS, PrimaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeL)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                        ) {
                            Text(
                                text = driver?.driverName ?: "Tài xế OmniGo",
                                style = MaterialTheme.typography.s16.bold(),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(id = R.string.ride_driver_rating_default),
                                style = MaterialTheme.typography.s12.semiBold(),
                                color = PrimaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.XXS))

                        // Vehicle plate badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.ShapeXS))
                                .background(OutlineVariantLight.copy(alpha = 0.3f))
                                .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeXS))
                                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = driver?.vehiclePlate ?: "Đang cập nhật biển số",
                                style = MaterialTheme.typography.s13.bold(),
                                color = TextPrimary
                            )
                        }

                        val vehicleDesc = listOfNotNull(driver?.vehicleType, driver?.vehicleModel).joinToString(" • ")
                        if (vehicleDesc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(AppSpacing.XXS))
                            Text(
                                text = vehicleDesc,
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Call Action Button
                Button(
                    onClick = { onCallDriver(driver?.driverPhone) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    modifier = Modifier.size(Dimen.SizeMega)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = stringResource(id = R.string.ride_call_driver_button),
                        tint = OnPrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTripRouteCard(booking: Booking) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingL)) {
            // Pickup
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.M))
                Column {
                    Text(
                        text = stringResource(id = R.string.ride_pickup_label),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(
                            id = R.string.driver_ride_offer_coordinates_format,
                            booking.startLatitude,
                            booking.startLongitude
                        ),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }
            }

            // Connector
            Box(
                modifier = Modifier
                    .padding(start = Dimen.PaddingS)
                    .width(Dimen.PaddingXXS)
                    .height(Dimen.PaddingL)
                    .background(OutlineVariantLight)
            )

            // Dropoff
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = ErrorColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.M))
                Column {
                    Text(
                        text = stringResource(id = R.string.ride_dropoff_label),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(
                            id = R.string.driver_ride_offer_coordinates_format,
                            booking.endLatitude,
                            booking.endLongitude
                        ),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // Fare & Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedPrice = String.format(Locale.getDefault(), "%,.0f đ", booking.price)
                Text(
                    text = "${stringResource(id = R.string.ride_fare_summary)}: $formattedPrice",
                    style = MaterialTheme.typography.s15.bold(),
                    color = PrimaryColor
                )

                Text(
                    text = stringResource(id = R.string.ride_distance_format, booking.distanceInKm),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextSecondary
                )
            }
        }
    }
}
