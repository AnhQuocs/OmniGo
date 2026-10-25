package com.example.omnigo.features.customer.ride.presentation.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
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
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun RideLiveTrackingCard(
    status: BookingStatus,
    driverLocation: DriverLiveLocation?,
    targetLatitude: Double?,
    targetLongitude: Double?,
    distanceKm: Double?,
    etaMinutes: Int?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingL)
        ) {
            // 1. Header: Live Tag & Dynamic Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(Dimen.SizeS)
                            .clip(CircleShape)
                            .background(SuccessColor.copy(alpha = pulseAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeS)
                            .clip(CircleShape)
                            .background(SuccessColor)
                    )
                    Text(
                        text = stringResource(id = R.string.ride_live_tracking_badge),
                        style = MaterialTheme.typography.s12.bold(),
                        color = SuccessColor
                    )
                }

                Text(
                    text = stringResource(id = R.string.ride_live_tracking_title),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Stage Sub-banner
            val stageTitle = when (status) {
                BookingStatus.ACCEPTED -> stringResource(id = R.string.ride_live_tracking_status_en_route)
                BookingStatus.ARRIVED -> stringResource(id = R.string.ride_live_tracking_status_arrived)
                BookingStatus.IN_PROGRESS -> stringResource(id = R.string.ride_live_tracking_status_in_progress)
                else -> stringResource(id = R.string.ride_live_tracking_status_waiting)
            }

            AnimatedContent(
                targetState = stageTitle,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "trackingStageAnimation"
            ) { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // 2. Metrics Row: ETA, Distance, Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ETA
                TrackingMetricItem(
                    icon = Icons.Default.Schedule,
                    label = stringResource(id = R.string.ride_live_tracking_eta_label),
                    value = formatEtaText(etaMinutes)
                )

                // Distance
                val distanceText = distanceKm?.let {
                    String.format(Locale.getDefault(), "%.1f km", it)
                } ?: "--"
                TrackingMetricItem(
                    icon = Icons.Default.NearMe,
                    label = stringResource(id = R.string.ride_live_tracking_distance_label),
                    value = distanceText
                )

                // Speed
                val speedKmh = driverLocation?.speed?.let { it * 3.6f }
                val speedText = if (speedKmh == null) {
                    "--"
                } else if (speedKmh < 3.0f) {
                    stringResource(id = R.string.ride_live_tracking_speed_stationary)
                } else {
                    stringResource(id = R.string.ride_live_tracking_speed_format, speedKmh)
                }
                TrackingMetricItem(
                    icon = Icons.Default.Speed,
                    label = stringResource(id = R.string.ride_live_tracking_speed_label),
                    value = speedText
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // 3. Visual Route & Dynamic Rotating Vehicle Indicator
            VisualRouteBar(
                driverLocation = driverLocation,
                status = status
            )

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // 4. Open External Google Maps Navigation Button
            Button(
                onClick = {
                    openGoogleMapsNavigation(
                        context = context,
                        targetLat = targetLatitude,
                        targetLng = targetLongitude
                    )
                },
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = OnPrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Text(
                    text = stringResource(id = R.string.ride_live_tracking_open_navigation),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = OnPrimaryColor
                )
            }
        }
    }
}

@Composable
private fun TrackingMetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeSM)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.XXS))
        Text(
            text = value,
            style = MaterialTheme.typography.s15.bold(),
            color = TextPrimary
        )
    }
}

@Composable
private fun VisualRouteBar(
    driverLocation: DriverLiveLocation?,
    status: BookingStatus
) {
    val animatedBearing by animateFloatAsState(
        targetValue = driverLocation?.bearing ?: 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bearingAnimation"
    )

    Card(
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Start node
            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .clip(CircleShape)
                    .background(if (status == BookingStatus.IN_PROGRESS) PrimaryColor else SuccessColor),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.PaddingS)
                        .clip(CircleShape)
                        .background(SurfaceLight)
                )
            }

            // Connector Track
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimen.PaddingS)
                    .height(Dimen.PaddingXXS)
                    .background(OutlineVariantLight)
            )

            // Dynamic Vehicle Node with Rotated Bearing Icon
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXL)
                    .clip(CircleShape)
                    .background(PrimaryColor)
                    .border(Dimen.PaddingXXS, PrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = OnPrimaryColor,
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .rotate(animatedBearing)
                )
            }

            // Connector Track
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimen.PaddingS)
                    .height(Dimen.PaddingXXS)
                    .background(OutlineVariantLight)
            )

            // Destination Node
            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .clip(CircleShape)
                    .background(SecondaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = OnPrimaryColor,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
            }
        }
    }
}

@Composable
private fun formatEtaText(etaMinutes: Int?): String {
    return when {
        etaMinutes == null -> "--"
        etaMinutes <= 1 -> stringResource(id = R.string.ride_live_tracking_eta_less_than_minute)
        else -> stringResource(id = R.string.ride_live_tracking_eta_min_format, etaMinutes)
    }
}

private fun openGoogleMapsNavigation(
    context: Context,
    targetLat: Double?,
    targetLng: Double?
) {
    if (targetLat == null || targetLng == null) return
    val navigationUri = Uri.parse("google.navigation:q=$targetLat,$targetLng")
    val mapIntent = Intent(Intent.ACTION_VIEW, navigationUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapIntent)
    } catch (_: ActivityNotFoundException) {
        val geoUri = Uri.parse("geo:$targetLat,$targetLng?q=$targetLat,$targetLng")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri)
        try {
            context.startActivity(fallbackIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.ride_live_tracking_nav_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
