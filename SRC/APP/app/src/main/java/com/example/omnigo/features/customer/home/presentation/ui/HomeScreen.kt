package com.example.omnigo.features.customer.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeFilterChipsSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeHeaderSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomePopularPlacesSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomePromoBannersSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeQuickWalletSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeSearchSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeServicesGridSection
import com.example.omnigo.features.main.presentation.ui.components.LocalBottomNavVisibility
import com.example.omnigo.features.main.presentation.ui.components.animateToolbarOffset
import com.example.omnigo.features.main.presentation.ui.components.rememberCollapsingBarsState
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.SurfaceLight

private val TOP_BAR_HEIGHT = 60.dp

@Composable
fun HomeScreen(
    onNavigateToService: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()
    val contentHeaderHeightPx = with(density) { TOP_BAR_HEIGHT.toPx() }
    val fullHeaderHeightPx = statusBarHeightPx + contentHeaderHeightPx

    val bottomNavController = LocalBottomNavVisibility.current
    val barsState = rememberCollapsingBarsState()

    val headerOffset = animateToolbarOffset(barsState.barsVisible, fullHeaderHeightPx)

    LaunchedEffect(barsState.barsVisible) {
        bottomNavController.visible = barsState.barsVisible
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .nestedScroll(barsState.nestedScrollConnection)
                .offset {
                    val visibleHeaderHeight = (fullHeaderHeightPx + headerOffset).coerceAtLeast(0f)
                    IntOffset(0, visibleHeaderHeight.toInt())
                }
                .verticalScroll(scrollState)
        ) {
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
                    // Handle place click/.
                }
            )

            Spacer(modifier = Modifier.height(AppSpacing.XXL))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .offset { IntOffset(0, headerOffset.toInt()) }
        ) {
            HomeHeaderSection(
                onNotificationClick = onNavigateToNotifications,
                modifier = Modifier.height(TOP_BAR_HEIGHT)
            )
        }
    }
}