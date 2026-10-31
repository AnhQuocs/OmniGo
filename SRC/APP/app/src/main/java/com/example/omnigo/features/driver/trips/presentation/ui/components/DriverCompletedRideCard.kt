package com.example.omnigo.features.driver.trips.presentation.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.driver.trips.domain.util.DriverTripsTimelineHelper
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverCompletedRideCard(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    val isCompleted = booking.status == BookingStatus.COMPLETED
    val badgeBg = if (isCompleted) SuccessColor.copy(alpha = 0.12f) else ErrorContainer
    val badgeColor = if (isCompleted) SuccessColor else ErrorColor
    val badgeTextRes = if (isCompleted) {
        R.string.driver_trips_history_ride_completed
    } else {
        R.string.driver_trips_history_ride_cancelled
    }

    val timeStr = DriverTripsTimelineHelper.parseTime(booking.completedAt ?: booking.createdAt)
    val netEarnings = booking.price * 0.80

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.BorderHairline,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header Row: Icon + Title + Time + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeL)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.S))

                    Column {
                        Text(
                            text = "${stringResource(id = R.string.driver_trips_completed_ride_title)} #${booking.bookingId}",
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextPrimary
                        )
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextTertiary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeS))
                        .background(badgeBg)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Text(
                        text = stringResource(id = badgeTextRes),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.SPlus))

            // Route Details: Pickup & Dropoff
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.XSPlus)
            ) {
                // Pickup
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = Dimen.PaddingXXS)
                            .size(Dimen.PaddingS)
                            .clip(CircleShape)
                            .background(SuccessColor)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = booking.pickupAddress.ifBlank { "Điểm đón khách" },
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dropoff
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = Dimen.PaddingXXS)
                            .size(Dimen.PaddingS)
                            .clip(CircleShape)
                            .background(PrimaryColor)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = booking.dropoffAddress.ifBlank { "Điểm trả khách" },
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.SPlus))
            HorizontalDivider(thickness = Dimen.BorderHairline, color = OutlineVariantLight.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(AppSpacing.SPlus))

            // Footer Row: Payment method, distance & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    val payLabel = if (booking.paymentMethod?.uppercase() == "WALLET") "Ví Omni" else "Tiền mặt"
                    Text(
                        text = payLabel,
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = TextSecondary
                    )

                    if (booking.distanceInKm > 0.0) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextTertiary
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f km", booking.distanceInKm),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextTertiary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                ) {
                    Text(
                        text = "${stringResource(id = R.string.driver_trips_net_earnings_label)}:",
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "+%,.0f ₫", netEarnings),
                        style = MaterialTheme.typography.s14.bold(),
                        color = PrimaryColor
                    )
                }
            }
        }
    }
}
