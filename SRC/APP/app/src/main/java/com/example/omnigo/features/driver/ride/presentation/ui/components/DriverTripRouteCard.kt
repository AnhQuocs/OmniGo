package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun DriverTripRouteCard(
    pickupAddress: String?,
    dropoffAddress: String?,
    startLat: Double?,
    startLng: Double?,
    endLat: Double?,
    endLng: Double?,
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .clip(CircleShape)
                        .background(SuccessColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SuccessColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.SPlus))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.driver_trip_pickup_title),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = pickupDisplay,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = Dimen.PaddingS, top = Dimen.PaddingXXS, bottom = Dimen.PaddingXXS)
                    .width(Dimen.PaddingXXS)
                    .height(Dimen.PaddingM)
                    .background(OutlineVariantLight)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .clip(CircleShape)
                        .background(SecondaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.SPlus))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.driver_trip_dropoff_title),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = dropoffDisplay,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
