package com.example.omnigo.features.driver.trips.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverCockpitCapsule
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverFoodOrderCard
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverPillTabSwitcher
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverTripsEmptyState
import com.example.omnigo.features.driver.trips.presentation.viewmodel.DriverTripsViewModel
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight

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

    // Tách riêng 2 ScrollState độc lập cho từng tab
    val activeScrollState = rememberScrollState()
    val historyScrollState = rememberScrollState()

    val activeCount = (if (uiState.activeBooking != null) 1 else 0) + uiState.activeFoodOrders.size
    val completedCount = (uiState.dailyReport?.totalTrips ?: 0) + uiState.historyFoodOrders.size

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
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingM)
        ) {
            // 1. PINNED HEADER AREA (Cố định ở trên đỉnh, không bị cuộn trôi)
            DriverRefreshTitleBar(
                title = stringResource(id = R.string.driver_nav_trips),
                onRefreshClick = viewModel::refresh,
                isRefreshEnabled = !uiState.isReportLoading
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Cockpit Radar Capsule (Cố định)
            DriverCockpitCapsule(
                activeCount = activeCount,
                completedCount = completedCount
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Floating Elevated Pill Tab Switcher (Cố định)
            DriverPillTabSwitcher(
                selectedTabIndex = selectedTabIndex,
                activeCount = activeCount,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // 2. INDEPENDENT SCROLLABLE CONTENT (Vùng cuộn độc lập theo từng tab)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTabIndex == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(activeScrollState),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
                    ) {
                        val activeRide = uiState.activeBooking
                        val activeFoods = uiState.activeFoodOrders

                        if (activeRide == null && activeFoods.isEmpty()) {
                            DriverTripsEmptyState(
                                message = stringResource(id = R.string.driver_no_active_trip)
                            )
                        } else {
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

                        Spacer(modifier = Modifier.height(AppSpacing.XXLPlus))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(historyScrollState),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
                    ) {
                        val historyFoods = uiState.historyFoodOrders

                        if (historyFoods.isEmpty()) {
                            DriverTripsEmptyState(
                                message = stringResource(id = R.string.driver_no_trip_history)
                            )
                        } else {
                            historyFoods.forEach { order ->
                                DriverFoodOrderCard(
                                    order = order,
                                    isActive = false,
                                    onResumeClick = {}
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.XXLPlus))
                    }
                }
            }
        }
    }
}
