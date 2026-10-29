package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s13
import java.util.Locale

@Composable
fun DriverTripGeofencePill(
    distanceToTargetMeters: Double?,
    isWithinGeofence: Boolean,
    modifier: Modifier = Modifier
) {
    val formattedDistance = if (distanceToTargetMeters == null) {
        stringResource(id = R.string.driver_delivery_gps_waiting)
    } else if (distanceToTargetMeters >= 1000.0) {
        String.format(Locale.US, "%.1f km", distanceToTargetMeters / 1000.0)
    } else {
        String.format(Locale.US, "%.0f m", distanceToTargetMeters)
    }

    val statusColor = if (isWithinGeofence) SuccessColor else WarningColor
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .background(statusColor.copy(alpha = 0.12f))
            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXSPlus),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isWithinGeofence) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(Dimen.SizeS2)
        )
        Spacer(modifier = Modifier.width(AppSpacing.XSPlus))
        Text(
            text = if (distanceToTargetMeters != null) {
                stringResource(R.string.driver_trip_distance_remaining, formattedDistance)
            } else {
                formattedDistance
            },
            style = MaterialTheme.typography.s13.bold(),
            color = statusColor
        )
    }
}

