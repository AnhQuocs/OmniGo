package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s18

@Composable
fun TripProgressCard(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    val statusText = when (booking.status) {
        BookingStatus.ACCEPTED -> stringResource(id = R.string.ride_stepper_accepted_title)
        BookingStatus.ARRIVED -> stringResource(id = R.string.ride_stepper_arrived_title)
        BookingStatus.IN_PROGRESS -> stringResource(id = R.string.ride_stepper_in_progress_title)
        BookingStatus.COMPLETED -> stringResource(id = R.string.ride_stepper_completed_title)
        else -> stringResource(id = R.string.ride_active_trip_title)
    }

    val hintText = when (booking.status) {
        BookingStatus.ACCEPTED -> stringResource(id = R.string.ride_status_accepted_hint)
        BookingStatus.ARRIVED -> stringResource(id = R.string.ride_status_arrived_hint)
        BookingStatus.IN_PROGRESS -> stringResource(id = R.string.ride_status_in_progress_hint)
        else -> ""
    }

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = PrimaryColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingL)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .clip(CircleShape)
                        .background(SuccessColor)
                )
                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        (fadeIn() + slideInVertically()).togetherWith(fadeOut() + slideOutVertically())
                    },
                    label = "tripStatusTitleAnimation"
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                }
            }

            if (hintText.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }
        }
    }
}
