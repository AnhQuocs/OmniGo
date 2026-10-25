package com.example.omnigo.features.customer.activity.presentation.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityEmptyStateSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityHeaderSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityListSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityTabsSection
import com.example.omnigo.features.customer.activity.presentation.viewmodel.ActivityViewModel
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.presentation.ui.activity.RideActivity
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor

private val TOP_BAR_HEIGHT = 60.dp

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(id = R.string.activity_tab_ongoing),
        stringResource(id = R.string.activity_tab_history)
    )

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    OmniGoMainLayout(
        modifier = modifier,
        headerHeight = TOP_BAR_HEIGHT,
        header = {
            ActivityHeaderSection()
        }
    ) { contentModifier ->
        Column(
            modifier = contentModifier.fillMaxSize()
        ) {
            ActivityTabsSection(
                tabTitles = tabTitles,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            val isHistory = selectedTabIndex == 1
            val currentList = if (isHistory) uiState.historyBookings else uiState.ongoingBookings

            if (uiState.isLoading && currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimen.PaddingXL),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else if (currentList.isEmpty()) {
                ActivityEmptyStateSection(
                    icon = if (isHistory) Icons.Filled.History else Icons.Filled.PendingActions,
                    message = if (isHistory) {
                        stringResource(id = R.string.activity_empty_history)
                    } else {
                        stringResource(id = R.string.activity_empty_ongoing)
                    }
                )
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    ActivityListSection(
                        bookings = currentList,
                        isHistory = isHistory,
                        onBookingClick = { booking: Booking ->
                            if (!isHistory) {
                                val intent = Intent(context, RideActivity::class.java).apply {
                                    putExtra("EXTRA_BOOKING_ID", booking.bookingId)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}
