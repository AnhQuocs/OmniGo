package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.WarningColor
import java.util.Locale

@Composable
fun ActivityListSection(
    bookings: List<Booking>,
    isHistory: Boolean,
    onBookingClick: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimen.PaddingM),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
    ) {
        bookings.forEach { booking ->
            val isCompleted = booking.status == BookingStatus.COMPLETED
            val isCancelled = booking.status == BookingStatus.CANCELLED
            
            val statusColor = when {
                isCompleted -> SuccessColor
                isCancelled -> ErrorColor
                else -> PrimaryColor
            }

            val statusText = when (booking.status) {
                BookingStatus.COMPLETED -> stringResource(R.string.activity_item_completed_badge)
                BookingStatus.CANCELLED -> stringResource(R.string.activity_item_cancelled_badge)
                else -> stringResource(R.string.activity_item_ongoing_badge)
            }

            val displayTitle = "${stringResource(R.string.home_service_ride)} #${booking.bookingId}"
            val displayPrice = String.format(Locale.getDefault(), "%,.0f đ", booking.price)
            val displayDate = stringResource(R.string.activity_mock_date_1)

            ActivityItemCard(
                type = ActivityType.RIDE,
                title = displayTitle,
                date = displayDate,
                price = displayPrice,
                status = statusText,
                isCompleted = isCompleted,
                statusColorOverride = statusColor,
                onClick = { onBookingClick(booking) }
            )
        }
    }
}
