package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverTripRouteCard(
    status: BookingStatus,
    pickupAddress: String?,
    dropoffAddress: String?,
    startLat: Double?,
    startLng: Double?,
    endLat: Double?,
    endLng: Double?,
    distanceToTargetMeters: Double?,
    isWithinGeofence: Boolean,
    modifier: Modifier = Modifier
) {
    val pickupDisplay = pickupAddress ?: if (startLat != null && startLng != null) {
        stringResource(R.string.driver_ride_offer_coordinates_format, startLat, startLng)
    } else {
        stringResource(R.string.driver_trip_pickup_title)
    }

    val dropoffDisplay = dropoffAddress ?: if (endLat != null && endLng != null) {
        stringResource(R.string.driver_ride_offer_coordinates_format, endLat, endLng)
    } else {
        stringResource(R.string.driver_trip_dropoff_title)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            ),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingML)
        ) {
            // Pickup Row
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .background(PrimaryColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(AppSpacing.M))
                Column {
                    Text(
                        text = stringResource(R.string.driver_trip_pickup_title),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                    Text(
                        text = pickupDisplay,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }
            }

            // Connecting line
            Box(
                modifier = Modifier
                    .padding(start = Dimen.PaddingS)
                    .width(Dimen.PaddingXXS)
                    .height(Dimen.SizeM)
                    .background(CardBorderColor)
            )

            // Dropoff Row
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .background(SecondaryColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(AppSpacing.M))
                Column {
                    Text(
                        text = stringResource(R.string.driver_trip_dropoff_title),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                    Text(
                        text = dropoffDisplay,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Distance & Geofencing status pill
            if (distanceToTargetMeters != null && status != BookingStatus.COMPLETED) {
                val formattedDistance = if (distanceToTargetMeters >= 1000.0) {
                    String.format(Locale.US, "%.1f km", distanceToTargetMeters / 1000.0)
                } else {
                    String.format(Locale.US, "%.0f m", distanceToTargetMeters)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isWithinGeofence) SuccessColor.copy(alpha = 0.12f) else WarningColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(AppShape.ShapeM)
                        )
                        .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isWithinGeofence) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isWithinGeofence) SuccessColor else WarningColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = stringResource(R.string.driver_trip_distance_remaining, formattedDistance),
                        style = MaterialTheme.typography.s13.bold(),
                        color = if (isWithinGeofence) SuccessColor else WarningColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(
                            if (isWithinGeofence) R.string.driver_trip_geofence_in_range
                            else R.string.driver_trip_geofence_out_of_range
                        ),
                        style = MaterialTheme.typography.s12.medium(),
                        color = if (isWithinGeofence) SuccessColor else WarningColor
                    )
                }
            }
        }
    }
}
