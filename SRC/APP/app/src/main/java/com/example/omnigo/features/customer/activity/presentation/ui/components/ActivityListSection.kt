package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onCancelRideClick: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingSM),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge)
    ) {
        items(
            items = ongoingFoodOrders,
            key = { "ongoing_food_${it.id}" }
        ) { order ->
            OngoingFoodActivityCard(
                order = order,
                onClick = { onFoodOrderClick(order) }
            )
        }

        items(
            items = ongoingBookings,
            key = { "ongoing_ride_${it.bookingId}" }
        ) { booking ->
            OngoingRideActivityCard(
                booking = booking,
                driverProfile = driverProfiles[booking.bookingId],
                activeRideState = activeRideState,
                onCardClick = { onRideBookingClick(booking) },
                onCallDriver = onCallDriver,
                onCancelRideClick = { onCancelRideClick(booking) }
            )
        }

        item(key = "ongoing_bottom_spacer") {
            Spacer(modifier = Modifier.height(Dimen.HeightBottomNavPadding))
        }
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
    val monthGroups = remember(historyFoodOrders) {
        ActivityHistoryHelper.groupFoodOrdersByMonth(historyFoodOrders)
    }
    val now = remember { LocalDate.now() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingS),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
    ) {
        when (selectedService) {
            HistoryServiceFilter.OMNI_FOOD -> {
                monthGroups.forEachIndexed { groupIndex, group ->
                    item(key = "food_header_${group.year}_${group.month}") {
                        if (groupIndex > 0) {
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
                    }

                    items(
                        items = group.items,
                        key = { "food_order_${it.id}" }
                    ) { order ->
                        ActivityCompactFoodHistoryCard(
                            order = order,
                            onClick = { onFoodOrderClick(order) }
                        )
                    }
                }
            }

            HistoryServiceFilter.OMNI_RIDE -> {
                item(key = "ride_header_now") {
                    Text(
                        text = stringResource(
                            R.string.activity_history_month_header,
                            now.monthValue,
                            now.year
                        ),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                }

                items(
                    items = historyBookings,
                    key = { "ride_booking_${it.bookingId}" }
                ) { booking ->
                    ActivityCompactRideHistoryCard(
                        booking = booking,
                        onClick = { onRideBookingClick(booking) }
                    )
                }
            }
        }

        item(key = "history_bottom_spacer") {
            Spacer(modifier = Modifier.height(Dimen.HeightBottomNavPadding))
        }
    }
}
