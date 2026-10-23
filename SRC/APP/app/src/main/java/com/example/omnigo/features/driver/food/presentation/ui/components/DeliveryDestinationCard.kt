package com.example.omnigo.features.driver.food.presentation.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
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
import com.example.omnigo.utils.semiBold

@Composable
fun DeliveryDestinationCard(
    isPickupStep: Boolean,
    name: String,
    address: String,
    phone: String? = null,
    distanceMeters: Double?,
    isWithinGeofence: Boolean,
    hasCoordinates: Boolean = true,
    modifier: Modifier = Modifier
) {
    val stepTitle = if (isPickupStep) {
        stringResource(id = R.string.driver_delivery_step_pickup)
    } else {
        stringResource(id = R.string.driver_delivery_step_dropoff)
    }

    val iconColor = if (isPickupStep) PrimaryColor else SuccessColor

    Card(
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stepTitle,
                    style = MaterialTheme.typography.s12.medium(),
                    color = iconColor
                )

                // Geofence status chip
                val geofenceText = when {
                    !hasCoordinates -> stringResource(id = R.string.driver_delivery_missing_coordinates)
                    distanceMeters == null -> stringResource(id = R.string.driver_delivery_gps_waiting)
                    isWithinGeofence -> stringResource(id = R.string.driver_delivery_geofence_ready)
                    else -> stringResource(
                        id = R.string.driver_delivery_geofence_distance,
                        distanceMeters.toInt()
                    )
                }

                val geofenceBg = when {
                    !hasCoordinates -> ErrorColor.copy(alpha = 0.15f)
                    distanceMeters == null -> PrimaryContainer.copy(alpha = 0.5f)
                    isWithinGeofence -> SuccessColor.copy(alpha = 0.15f)
                    else -> ErrorColor.copy(alpha = 0.15f)
                }

                val geofenceColor = when {
                    !hasCoordinates -> ErrorColor
                    distanceMeters == null -> TextSecondary
                    isWithinGeofence -> SuccessColor
                    else -> ErrorColor
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeS))
                        .background(geofenceBg)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Text(
                        text = geofenceText,
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = geofenceColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { stringResource(id = R.string.driver_delivery_default_name) },
                        style = MaterialTheme.typography.s15.semiBold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (address.isNotBlank()) {
                        Text(
                            text = address,
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!phone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(Dimen.SizeSM)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.s12.medium(),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
