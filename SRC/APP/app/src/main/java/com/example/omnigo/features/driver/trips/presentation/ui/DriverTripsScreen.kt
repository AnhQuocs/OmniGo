package com.example.omnigo.features.driver.trips.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.common.ui.components.DriverRefreshTitleBar
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverActiveBookingCard
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverDailyReportCard
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverFoodOrderCard
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverTripsEmptyState
import com.example.omnigo.features.driver.trips.presentation.viewmodel.DriverTripsViewModel
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTripsScreen(
    modifier: Modifier = Modifier,
    viewModel: DriverTripsViewModel = hiltViewModel(),
    onResumeActiveTrip: (Booking) -> Unit = {},
    onResumeActiveFoodOrder: (DriverDeliveryOrder) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(id = R.string.driver_trips_tab_active),
        stringResource(id = R.string.driver_trips_tab_history)
    )

    val scrollState = rememberScrollState()

    PullToRefreshBox(
        isRefreshing = uiState.isReportLoading,
        onRefresh = viewModel::refresh,
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingM)
    ) {
        DriverRefreshTitleBar(
            title = stringResource(id = R.string.driver_nav_trips),
            onRefreshClick = viewModel::refresh,
            isRefreshEnabled = !uiState.isReportLoading
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DriverDailyReportCard(
            report = uiState.dailyReport,
            isLoading = uiState.isReportLoading
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SurfaceLight,
            contentColor = PrimaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PrimaryColor,
                    height = Dimen.PaddingXXS
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = if (selectedTabIndex == index) {
                                MaterialTheme.typography.s14.semiBold()
                            } else {
                                MaterialTheme.typography.s14.normal()
                            },
                            color = if (selectedTabIndex == index) PrimaryColor else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.L))

        if (selectedTabIndex == 0) {
            val activeRide = uiState.activeBooking
            val activeFoods = uiState.activeFoodOrders

            if (activeRide == null && activeFoods.isEmpty()) {
                DriverTripsEmptyState(
                    message = stringResource(id = R.string.driver_no_active_trip)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.M)) {
                    activeRide?.let { booking ->
                        DriverActiveBookingCard(
                            booking = booking,
                            onResumeClick = { onResumeActiveTrip(booking) }
                        )
                    }
                    activeFoods.forEach { foodOrder ->
                        DriverFoodOrderCard(
                            order = foodOrder,
                            isActive = true,
                            onResumeClick = { onResumeActiveFoodOrder(foodOrder) }
                        )
                    }
                }
            }
        } else {
            val historyFoods = uiState.historyFoodOrders
            if (historyFoods.isEmpty()) {
                DriverTripsEmptyState(
                    message = stringResource(id = R.string.driver_no_trip_history)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.M)) {
                    historyFoods.forEach { order ->
                        DriverFoodOrderCard(
                            order = order,
                            isActive = false,
                            onResumeClick = {}
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.XXLPlus))
    }
    }
}
