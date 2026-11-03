package com.example.omnigo.features.driver.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.driver.profile.presentation.ui.components.EmptyReviewsBox
import com.example.omnigo.features.driver.profile.presentation.ui.components.FoodDeliveryReviewItemCard
import com.example.omnigo.features.driver.profile.presentation.ui.components.HeroRatingSummaryCard
import com.example.omnigo.features.driver.profile.presentation.ui.components.RideReviewItemCard
import com.example.omnigo.features.driver.profile.presentation.ui.components.StarFilterRow
import com.example.omnigo.features.driver.profile.presentation.viewmodel.DriverReviewsViewModel
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18

@Composable
fun DriverReviewsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverReviewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight)
                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back),
                    tint = TextPrimary
                )
            }
            Text(
                text = stringResource(id = R.string.driver_reviews_screen_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.loadAllReviews() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(id = R.string.common_retry),
                    tint = PrimaryColor
                )
            }
        }

        // Dual Tabs (Cuốc xe vs Đồ ăn)
        TabRow(
            selectedTabIndex = uiState.selectedTabIndex,
            containerColor = SurfaceLight,
            contentColor = PrimaryColor
        ) {
            Tab(
                selected = uiState.selectedTabIndex == 0,
                onClick = { viewModel.onTabSelected(0) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.driver_reviews_tab_ride),
                            style = MaterialTheme.typography.s14.medium()
                        )
                    }
                }
            )
            Tab(
                selected = uiState.selectedTabIndex == 1,
                onClick = { viewModel.onTabSelected(1) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.driver_reviews_tab_food),
                            style = MaterialTheme.typography.s14.medium()
                        )
                    }
                }
            )
        }

        if (uiState.isLoading && uiState.rideReviews.isEmpty() && uiState.foodReviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimen.PaddingL),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimen.PaddingM),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.s14.normal(),
                            color = ErrorColor,
                            modifier = Modifier.padding(bottom = Dimen.PaddingS)
                        )
                    }
                }

                // Rating Hero Card
                item {
                    HeroRatingSummaryCard(uiState = uiState)
                }

                // Filter by star
                item {
                    StarFilterRow(
                        selectedStar = uiState.selectedStarFilter,
                        onSelectStar = { viewModel.onStarFilterSelected(it) }
                    )
                }

                // Review items
                if (uiState.selectedTabIndex == 0) {
                    val list = uiState.filteredRideReviews
                    if (list.isEmpty()) {
                        item { EmptyReviewsBox() }
                    } else {
                        items(list, key = { it.id }) { review ->
                            RideReviewItemCard(review = review)
                        }
                    }
                } else {
                    val list = uiState.filteredFoodReviews
                    if (list.isEmpty()) {
                        item { EmptyReviewsBox() }
                    } else {
                        items(list, key = { it.id }) { review ->
                            FoodDeliveryReviewItemCard(review = review)
                        }
                    }
                }
            }
        }
    }
}
