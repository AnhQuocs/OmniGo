package com.example.omnigo.features.driver.trips.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverDailyReportCard
import com.example.omnigo.features.driver.trips.presentation.viewmodel.DriverTripsViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnSurfaceVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverTripsScreen(
    modifier: Modifier = Modifier,
    viewModel: DriverTripsViewModel = hiltViewModel(),
    onResumeActiveTrip: (Booking) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(id = R.string.driver_trips_tab_active),
        stringResource(id = R.string.driver_trips_tab_history)
    )

    LaunchedEffect(Unit) {
        viewModel.loadDailyReport()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        Text(
            text = stringResource(id = R.string.driver_nav_trips),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Daily Revenue Report
        DriverDailyReportCard(
            report = uiState.dailyReport,
            isLoading = uiState.isReportLoading
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // Tabs
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
            val active = uiState.activeBooking
            if (active != null) {
                DriverActiveBookingCard(
                    booking = active,
                    onResumeClick = { onResumeActiveTrip(active) }
                )
            } else {
                DriverTripsEmptyState(
                    message = stringResource(id = R.string.driver_no_active_trip)
                )
            }
        } else {
            DriverTripsEmptyState(
                message = stringResource(id = R.string.driver_no_trip_history)
            )
        }
    }
}

@Composable
private fun DriverActiveBookingCard(
    booking: Booking,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = PrimaryColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Text(
                    text = stringResource(id = R.string.driver_trips_active_trip_title),
                    style = MaterialTheme.typography.s14.bold(),
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            Text(
                text = "${stringResource(id = R.string.driver_trip_dropoff_title)} #${booking.bookingId}",
                style = MaterialTheme.typography.s16.semiBold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            val priceFormatted = String.format(Locale.getDefault(), "%,.0f đ", booking.price)
            Text(
                text = priceFormatted,
                style = MaterialTheme.typography.s14.bold(),
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Button(
                onClick = onResumeClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(AppShape.ShapeM),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.driver_trips_btn_resume_trip),
                    style = MaterialTheme.typography.s14.semiBold()
                )
            }
        }
    }
}

@Composable
private fun DriverTripsEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_activity),
                contentDescription = null,
                tint = OnSurfaceVariantLight,
                modifier = Modifier.size(Dimen.SizeXXL)
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = message,
                style = MaterialTheme.typography.s15.normal(),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
