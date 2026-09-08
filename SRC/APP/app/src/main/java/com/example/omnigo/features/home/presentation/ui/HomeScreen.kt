package com.example.omnigo.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.theme.BackgroundLight

@Composable
fun HomeScreen(
    onNavigateToService: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(scrollState)
    ) {
        HomeHeaderSection(
            onNotificationClick = onNavigateToNotifications
        )

        Spacer(modifier = Modifier.height(AppSpacing.XS))

        HomeQuickWalletSection()

        Spacer(modifier = Modifier.height(AppSpacing.M))

        HomeSearchSection(
            onSearchClick = onNavigateToSearch
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        HomeFilterChipsSection()

        Spacer(modifier = Modifier.height(AppSpacing.L))

        HomeServicesGridSection(
            onServiceClick = onNavigateToService
        )

        Spacer(modifier = Modifier.height(AppSpacing.LPlus))

        HomePromoBannersSection(
            onBannerClick = { promoCode ->
                // Handle promo click
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.LPlus))

        HomePopularPlacesSection(
            onPlaceClick = { placeId ->
                // Handle place click
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.XXL))
    }
}
