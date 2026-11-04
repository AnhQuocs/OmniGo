package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.activity.domain.util.ActivityHistoryHelper
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.formatVnd
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s24
import com.example.omnigo.utils.semiBold

@Composable
internal fun RideDetailSectionCard(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
            }
            content()
        }
    }
}

/** Header: loại xe + mã chuyến + thời gian, trạng thái và tổng tiền nổi bật. */
@Composable
internal fun RideDetailHeader(booking: Booking) {
    val dateText = ActivityHistoryHelper.formatCompactDateAndTime(booking.createdAt)
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.M)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RideVehicleBadge(vehicleType = booking.vehicleType)
            Spacer(modifier = Modifier.width(AppSpacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.ride_history_card_title,
                        rideVehicleLabel(booking.vehicleType),
                        booking.bookingId
                    ),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (dateText.isNotBlank()) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppSpacing.S))
            RideStatusChip(status = booking.status)
        }
        Text(
            text = booking.finalPrice.formatVnd(),
            style = MaterialTheme.typography.s24.bold(),
            color = PrimaryColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun RideDetailRouteCard(booking: Booking) {
    RideDetailSectionCard(title = stringResource(R.string.ride_history_route_section)) {
        RideRouteTimeline(
            pickup = booking.pickupAddress,
            dropoff = booking.dropoffAddress,
            pickupLabel = stringResource(R.string.ride_history_pickup_label),
            dropoffLabel = stringResource(R.string.ride_history_dropoff_label),
            maxLines = 3
        )
    }
}

@Composable
internal fun RideDetailInfoCard(booking: Booking) {
    val createdText = ActivityHistoryHelper.formatCompactDateAndTime(booking.createdAt)
    val completedText = ActivityHistoryHelper.formatCompactDateAndTime(booking.completedAt)
    RideDetailSectionCard(title = stringResource(R.string.ride_history_info_section)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
            RideInfoRow(
                label = stringResource(R.string.ride_history_vehicle_label),
                value = rideVehicleLabel(booking.vehicleType)
            )
            if (booking.distanceInKm > 0.0) {
                RideInfoRow(
                    label = stringResource(R.string.ride_history_distance_label),
                    value = stringResource(R.string.ride_trip_distance_format, booking.distanceInKm)
                )
            }
            RideInfoRow(
                label = stringResource(R.string.ride_history_payment_label),
                value = ridePaymentLabel(booking.paymentMethod)
            )
            if (createdText.isNotBlank()) {
                RideInfoRow(
                    label = stringResource(R.string.ride_history_booked_at_label),
                    value = createdText
                )
            }
            if (completedText.isNotBlank()) {
                RideInfoRow(
                    label = stringResource(R.string.ride_history_completed_at_label),
                    value = completedText
                )
            }
        }
    }
}

@Composable
internal fun RideDetailDriverCard(booking: Booking) {
    RideDetailSectionCard(title = stringResource(R.string.ride_detail_driver_section)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXXL)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!booking.driverAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(booking.driverAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = booking.driverName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppSpacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = booking.driverName.orEmpty(),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    booking.driverRating?.takeIf { it > 0.0 }?.let { rating ->
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = RatingGold,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = stringResource(R.string.ride_history_rating_format, rating),
                            style = MaterialTheme.typography.s13.semiBold(),
                            color = TextPrimary
                        )
                    }
                }
                val plate = booking.driverVehiclePlate
                if (!plate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = plate,
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = TextSecondary
                    )
                }
                if (!booking.driverPhone.isNullOrBlank()) {
                    Text(
                        text = booking.driverPhone,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
internal fun RideDetailFareCard(booking: Booking) {
    val hasDiscount = booking.discountAmount > 0.0
    RideDetailSectionCard(title = stringResource(R.string.ride_history_fare_section)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
            RideInfoRow(
                label = stringResource(R.string.ride_history_fare_label),
                value = booking.price.formatVnd()
            )
            if (hasDiscount) {
                val code = booking.voucherCode
                RideInfoRow(
                    label = if (code.isNullOrBlank()) {
                        stringResource(R.string.ride_history_fare_discount)
                    } else {
                        stringResource(R.string.voucher_discount_row, code)
                    },
                    value = "-${booking.discountAmount.formatVnd()}"
                )
            }
        }
        HorizontalDivider(color = CardBorderColor, thickness = Dimen.BorderHairline)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ride_history_fare_total),
                style = MaterialTheme.typography.s14.bold(),
                color = TextPrimary
            )
            Text(
                text = booking.finalPrice.formatVnd(),
                style = MaterialTheme.typography.s18.bold(),
                color = if (hasDiscount) SuccessColor else PrimaryColor
            )
        }
    }
}

