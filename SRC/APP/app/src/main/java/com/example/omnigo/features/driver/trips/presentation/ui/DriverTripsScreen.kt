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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnSurfaceVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun DriverTripsScreen(
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(id = R.string.driver_trips_tab_active),
        stringResource(id = R.string.driver_trips_tab_history)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        Text(
            text = stringResource(id = R.string.driver_nav_trips),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

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
            DriverTripsEmptyState(
                message = stringResource(id = R.string.driver_no_active_trip)
            )
        } else {
            DriverTripsEmptyState(
                message = stringResource(id = R.string.driver_no_trip_history)
            )
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
