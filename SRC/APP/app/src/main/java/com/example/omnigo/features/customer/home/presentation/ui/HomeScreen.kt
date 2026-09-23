package com.example.omnigo.features.customer.home.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeFilterChipsSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeHeaderSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomePopularPlacesSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomePromoBannersSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeQuickWalletSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeSearchSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeServicesGridSection
import com.example.omnigo.features.customer.home.presentation.viewmodel.HomeViewModel
import com.example.omnigo.ui.dimens.AppSpacing

private val TOP_BAR_HEIGHT = 60.dp

@Composable
fun HomeScreen(
    onNavigateToService: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isPopularPlacesLoading && uiState.popularPlaces.isEmpty()) {
        HomeScreenShimmer()
    } else {
        OmniGoMainLayout(
            modifier = modifier,
            headerHeight = TOP_BAR_HEIGHT,
            isRefreshing = uiState.isPopularPlacesLoading,
            onRefresh = viewModel::refresh,
            header = {
                HomeHeaderSection(
                    onNotificationClick = onNavigateToNotifications,
                    modifier = Modifier.height(TOP_BAR_HEIGHT)
                )
            }
        ) { contentModifier ->
            val scrollState = rememberScrollState()

            Column(
                modifier = contentModifier
                    .fillMaxSize()
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
                    popularRestaurants = uiState.popularPlaces,
                    onPlaceClick = { placeId ->
                        // Handle place click
                    },
                    onViewAllClick = {
                        onNavigateToService("food")
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.XXL))
            }
        }
    }
}