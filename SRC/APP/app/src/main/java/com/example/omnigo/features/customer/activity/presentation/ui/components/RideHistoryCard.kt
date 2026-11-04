package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.activity.domain.util.ActivityHistoryHelper
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.formatVnd
import com.example.omnigo.utils.lineThrough
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

/**
 * Card chuyến xe trong tab Lịch sử: Header (loại xe + mã + thời gian + trạng thái),
 * lộ trình timeline, và footer (quãng đường • thanh toán, tổng tiền).
 */
@Composable
fun ActivityCompactRideHistoryCard(
    booking: Booking,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCancelled = booking.status == BookingStatus.CANCELLED
    val paymentLabel = ridePaymentLabel(booking.paymentMethod)
    val summary = if (booking.distanceInKm > 0.0) {
        stringResource(R.string.activity_history_ride_summary, booking.distanceInKm, paymentLabel)
    } else {
        paymentLabel
    }
    val dateText = ActivityHistoryHelper.formatCompactDateAndTime(
        booking.completedAt ?: booking.createdAt
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationXS),
        border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
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
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (dateText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.width(AppSpacing.S))
                RideStatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            RideRouteTimeline(
                pickup = booking.pickupAddress,
                dropoff = booking.dropoffAddress
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = CardBorderColor, thickness = Dimen.BorderHairline)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Text(
                    text = booking.finalPrice.formatVnd(),
                    style = if (isCancelled) {
                        MaterialTheme.typography.s14.normal().lineThrough()
                    } else {
                        MaterialTheme.typography.s16.bold()
                    },
                    color = if (isCancelled) TextTertiary else PrimaryColor,
                    maxLines = 1
                )
            }
        }
    }
}

