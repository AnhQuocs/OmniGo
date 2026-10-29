package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun OngoingRideActivityCard(
    booking: Booking,
    driverProfile: BookingDriverProfile?,
    activeRideState: ActiveRideState?,
    onCardClick: () -> Unit,
    onCallDriver: (String?) -> Unit,
    onMessageDriver: (String?) -> Unit,
    onCancelRideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveStatus = if (activeRideState?.bookingId == booking.bookingId) {
        activeRideState.status
    } else {
        booking.status
    }

    val effectiveDriver = driverProfile ?: activeRideState?.takeIf { it.bookingId == booking.bookingId }?.driverProfile
    val driverName = effectiveDriver?.driverName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.ride_driver_name_fallback)
    val driverAvatar = effectiveDriver?.avatarUrl?.takeIf { it.isNotBlank() }
        ?: booking.driverAvatarUrl.orEmpty()
    val driverPhone = effectiveDriver?.driverPhone

    val plateText = effectiveDriver?.vehiclePlate?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.ride_vehicle_plate_fallback)
    val modelText = effectiveDriver?.vehicleModel?.takeIf { it.isNotBlank() }
        ?: effectiveDriver?.vehicleType?.takeIf { it.isNotBlank() }
        ?: booking.vehicleType.orEmpty()
    val vehicleLineText = listOfNotNull(
        plateText.takeIf { it.isNotBlank() },
        modelText.takeIf { it.isNotBlank() }
    ).joinToString(" • ")

    val distanceKm = activeRideState?.takeIf { it.bookingId == booking.bookingId }?.liveDistanceKm
        ?: booking.distanceInKm.takeIf { it > 0.0 }
        ?: 1.2
    val etaMins = activeRideState?.takeIf { it.bookingId == booking.bookingId }?.liveEtaMinutes
        ?: (distanceKm * 2.5).toInt().coerceAtLeast(3)

    val statusText = when (effectiveStatus) {
        BookingStatus.PENDING -> stringResource(R.string.activity_ride_status_searching)
        BookingStatus.ACCEPTED -> stringResource(R.string.activity_ride_status_driver_arriving)
        BookingStatus.ARRIVED -> stringResource(R.string.activity_ride_status_arrived)
        BookingStatus.IN_PROGRESS -> stringResource(R.string.activity_ride_status_in_progress)
        else -> stringResource(R.string.activity_ride_status_driver_arriving)
    }

    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL)),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header: OmniRide Service Chip & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeXL))
                        .background(PrimaryContainer)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(R.string.activity_service_omniride),
                        style = MaterialTheme.typography.s12.bold(),
                        color = PrimaryColor
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeXL))
                        .background(SuccessColor.copy(alpha = 0.14f))
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessColor,
                        modifier = Modifier.size(Dimen.SizeIconXS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.s12.bold(),
                        color = SuccessColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Driver Info Row + Call & Chat Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXXL)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (driverAvatar.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(driverAvatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = driverName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeL)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = stringResource(R.string.activity_ride_driver_rating_trips),
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { onCallDriver(driverPhone) },
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(SurfaceVariantLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = stringResource(R.string.active_trip_sheet_call_driver),
                        tint = TextPrimary,
                        modifier = Modifier.size(Dimen.SizeS2)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                IconButton(
                    onClick = { onMessageDriver(driverPhone) },
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(SurfaceVariantLight)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(Dimen.SizeS2)
                    )
                }
            }

            if (vehicleLineText.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Text(
                    text = vehicleLineText,
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Stylized Mini Map Preview
            RideMiniMapPreview(
                etaMinutes = etaMins,
                distanceKm = distanceKm
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Pickup & Drop-off Route Section
            OngoingRideAddressesSection(
                pickupAddress = booking.pickupAddress,
                dropoffAddress = booking.dropoffAddress
            )

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // Footer Actions: "Hủy chuyến" & "Liên hệ tài xế"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                Button(
                    onClick = onCancelRideClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.SizeXLPlus),
                    shape = RoundedCornerShape(AppShape.ShapeXL),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceVariantLight,
                        contentColor = TextPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = Dimen.PaddingXXS / 4)
                ) {
                    Text(
                        text = stringResource(R.string.activity_ride_btn_cancel),
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = TextPrimary
                    )
                }

                OutlinedButton(
                    onClick = { onCallDriver(driverPhone) },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.SizeXLPlus),
                    shape = RoundedCornerShape(AppShape.ShapeXL),
                    border = BorderStroke(Dimen.PaddingXXS / 2, SecondaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryColor)
                ) {
                    Text(
                        text = stringResource(R.string.activity_ride_btn_contact_driver),
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = SecondaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun RideMiniMapPreview(
    etaMinutes: Int,
    distanceKm: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.HeightRideMiniMapPreview)
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .background(SurfaceVariantLight)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle background road network lines
            val gridStroke = Stroke(width = 10f, cap = StrokeCap.Round)
            drawLine(
                color = SurfaceLight,
                start = Offset(0f, h * 0.35f),
                end = Offset(w, h * 0.55f),
                strokeWidth = gridStroke.width
            )
            drawLine(
                color = SurfaceLight,
                start = Offset(w * 0.25f, 0f),
                end = Offset(w * 0.18f, h),
                strokeWidth = gridStroke.width
            )
            drawLine(
                color = SurfaceLight,
                start = Offset(w * 0.72f, 0f),
                end = Offset(w * 0.65f, h),
                strokeWidth = gridStroke.width
            )

            // Active blue route path
            val startPt = Offset(w * 0.22f, h * 0.42f)
            val midPt = Offset(w * 0.42f, h * 0.80f)
            val endPt = Offset(w * 0.76f, h * 0.45f)

            val routePath = Path().apply {
                moveTo(startPt.x, startPt.y)
                lineTo(midPt.x, midPt.y)
                lineTo(endPt.x, endPt.y)
            }
            drawPath(
                path = routePath,
                color = PrimaryColor,
                style = Stroke(
                    width = 9f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Origin dot under vehicle marker
            drawCircle(
                color = TextPrimary,
                radius = 7f,
                center = startPt
            )

            // Destination orange pin with white halo
            drawCircle(
                color = SurfaceLight,
                radius = 16f,
                center = endPt
            )
            drawCircle(
                color = SecondaryColor,
                radius = 11f,
                center = endPt
            )
        }

        // Driver Blue Pin Icon
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = Dimen.PaddingXXL, top = Dimen.PaddingS)
                .size(Dimen.SizeL)
                .clip(CircleShape)
                .background(PrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TwoWheeler,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(Dimen.SizeS2)
            )
        }

        // Dark ETA & Distance Tooltip Pill
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(AppShape.ShapeS))
                .background(TextPrimary)
                .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.activity_ride_map_eta_mins, etaMinutes),
                style = MaterialTheme.typography.s12.bold(),
                color = TextWhite
            )
            Text(
                text = stringResource(R.string.activity_ride_map_dist_km, distanceKm),
                style = MaterialTheme.typography.s10.normal(),
                color = OutlineVariantLight
            )
        }
    }
}

@Composable
private fun OngoingRideAddressesSection(
    pickupAddress: String,
    dropoffAddress: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeS)
                    .clip(CircleShape)
                    .background(OutlineVariantLight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.PaddingS)
                        .clip(CircleShape)
                        .background(TextPrimary)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.activity_ride_pickup_label),
                    style = MaterialTheme.typography.s10.normal(),
                    color = TextSecondary
                )
                Text(
                    text = pickupAddress.ifBlank { "—" },
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(start = Dimen.PaddingS - Dimen.PaddingXXS / 2)
                .width(Dimen.PaddingXXS)
                .height(Dimen.PaddingSM)
                .background(OutlineVariantLight)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeS)
                    .clip(CircleShape)
                    .background(SecondaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.PaddingS)
                        .clip(CircleShape)
                        .background(SecondaryColor)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.activity_ride_dropoff_label),
                    style = MaterialTheme.typography.s10.normal(),
                    color = TextSecondary
                )
                Text(
                    text = dropoffAddress.ifBlank { "—" },
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

