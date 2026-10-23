package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.DriverLocation
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import java.util.Locale

@Composable
fun OrderLiveTrackingCard(
    restaurantName: String?,
    restaurantLatitude: Double?,
    restaurantLongitude: Double?,
    dropOffAddress: String?,
    dropOffLatitude: Double?,
    dropOffLongitude: Double?,
    driverLocation: DriverLocation?,
    status: String,
    modifier: Modifier = Modifier
) {
    val normalizedStatus = status.uppercase(Locale.ROOT)
    val isTrackingRelevant = normalizedStatus in setOf(
        "DRIVER_ASSIGNED",
        "PICKED_UP",
        "DELIVERING",
        "ARRIVED_CUSTOMER",
        "DELIVERED"
    ) || driverLocation != null

    if (!isTrackingRelevant && restaurantLatitude == null && dropOffLatitude == null) {
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_tracking")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header with Live Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (driverLocation != null) PrimaryColor else TextSecondary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.food_tracking_title),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                }

                if (driverLocation != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShape.ShapeXS))
                            .background(PrimaryColor.copy(alpha = 0.12f))
                            .padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
                    ) {
                        Text(
                            text = stringResource(id = R.string.food_tracking_live_active),
                            style = MaterialTheme.typography.s12.bold(),
                            color = PrimaryColor.copy(alpha = pulseAlpha)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Step 1: Restaurant Location
            TrackingWaypointRow(
                icon = Icons.Default.Restaurant,
                iconTint = SecondaryColor,
                title = restaurantName ?: stringResource(id = R.string.food_tracking_restaurant_default),
                subtitle = if (restaurantLatitude != null && restaurantLongitude != null) {
                    "%.4f, %.4f".format(restaurantLatitude, restaurantLongitude)
                } else {
                    stringResource(id = R.string.food_tracking_no_coords)
                }
            )

            // Step 2: Driver Live Location (if active)
            AnimatedVisibility(
                visible = driverLocation != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .padding(start = Dimen.PaddingSM)
                            .width(Dimen.PaddingXXS)
                            .height(Dimen.PaddingML)
                            .background(CardBorderColor)
                    )
                    driverLocation?.let { loc ->
                        TrackingWaypointRow(
                            icon = Icons.Default.DeliveryDining,
                            iconTint = PrimaryColor,
                            title = stringResource(id = R.string.food_tracking_driver_moving),
                            subtitle = buildString {
                                append("%.4f, %.4f".format(loc.latitude, loc.longitude))
                                loc.speed?.let { spd ->
                                    append(" • %.1f km/h".format(spd * 3.6))
                                }
                            },
                            trailingIcon = loc.bearing?.let { bearing ->
                                {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = PrimaryColor,
                                        modifier = Modifier
                                            .size(Dimen.SizeS)
                                            .rotate(bearing.toFloat())
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Connecting line
            Box(
                modifier = Modifier
                    .padding(start = Dimen.PaddingSM)
                    .width(Dimen.PaddingXXS)
                    .height(Dimen.PaddingML)
                    .background(CardBorderColor)
            )

            // Step 3: Customer Drop-off
            TrackingWaypointRow(
                icon = Icons.Default.LocationOn,
                iconTint = PrimaryColor,
                title = dropOffAddress ?: stringResource(id = R.string.food_tracking_destination_default),
                subtitle = if (dropOffLatitude != null && dropOffLongitude != null) {
                    "%.4f, %.4f".format(dropOffLatitude, dropOffLongitude)
                } else {
                    stringResource(id = R.string.food_tracking_destination_hint)
                }
            )
        }
    }
}

@Composable
private fun TrackingWaypointRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeSM)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(Dimen.SizeS)
            )
        }

        Spacer(modifier = Modifier.width(AppSpacing.S))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.s13.medium(),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(AppSpacing.XS))
            trailingIcon()
        }
    }
}
