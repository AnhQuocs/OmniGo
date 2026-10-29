package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.activity.domain.util.ActivityHistoryHelper
import com.example.omnigo.features.customer.activity.presentation.viewmodel.HistoryServiceFilter
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s14
import java.time.LocalDate

@Composable
fun ActivityOngoingListSection(
    ongoingFoodOrders: List<FoodOrder>,
    ongoingBookings: List<Booking>,
    driverProfiles: Map<Long, BookingDriverProfile>,
    activeRideState: ActiveRideState?,
    onFoodOrderClick: (FoodOrder) -> Unit,
    onRideBookingClick: (Booking) -> Unit,
    onCallDriver: (String?) -> Unit,
    onMessageDriver: (String?) -> Unit,
    onCancelRideClick: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingSM),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge)
    ) {
        ongoingFoodOrders.forEach { order ->
            OngoingFoodActivityCard(
                order = order,
                onClick = { onFoodOrderClick(order) }
            )
        }

        ongoingBookings.forEach { booking ->
            OngoingRideActivityCard(
                booking = booking,
                driverProfile = driverProfiles[booking.bookingId],
                activeRideState = activeRideState,
                onCardClick = { onRideBookingClick(booking) },
                onCallDriver = onCallDriver,
                onMessageDriver = onMessageDriver,
                onCancelRideClick = { onCancelRideClick(booking) }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.Ultra))
    }
}

@Composable
fun ActivityHistoryListSection(
    selectedService: HistoryServiceFilter,
    historyFoodOrders: List<FoodOrder>,
    historyBookings: List<Booking>,
    onFoodOrderClick: (FoodOrder) -> Unit,
    onRideBookingClick: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingS),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
    ) {
        when (selectedService) {
            HistoryServiceFilter.OMNI_FOOD -> {
                val monthGroups = remember(historyFoodOrders) {
                    ActivityHistoryHelper.groupFoodOrdersByMonth(historyFoodOrders)
                }

                monthGroups.forEachIndexed { index, group ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(AppSpacing.XS))
                    }
                    Text(
                        text = stringResource(
                            R.string.activity_history_month_header,
                            group.month,
                            group.year
                        ),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )

                    group.items.forEach { order ->
                        ActivityCompactFoodHistoryCard(
                            order = order,
                            onClick = { onFoodOrderClick(order) }
                        )
                    }
                }
            }

            HistoryServiceFilter.OMNI_RIDE -> {
                val now = remember { LocalDate.now() }
                Text(
                    text = stringResource(
                        R.string.activity_history_month_header,
                        now.monthValue,
                        now.year
                    ),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )

                historyBookings.forEach { booking ->
                    ActivityCompactRideHistoryCard(
                        booking = booking,
                        onClick = { onRideBookingClick(booking) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.Ultra))
    }
}
