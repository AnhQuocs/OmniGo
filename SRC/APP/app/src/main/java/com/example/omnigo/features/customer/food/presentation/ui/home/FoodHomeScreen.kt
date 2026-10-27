package com.example.omnigo.features.customer.food.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.ui.components.CartBottomBar
import com.example.omnigo.features.customer.food.presentation.viewmodel.FoodUiState
import com.example.omnigo.features.customer.food.presentation.viewmodel.FoodViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun FoodHomeScreen(
    onBackClick: () -> Unit,
    onRestaurantClick: (Long) -> Unit,
    onHistoryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FoodHeaderSection(
                onBackClick = onBackClick,
                userAddressLabel = uiState.userAddressLabel,
                onHistoryClick = onHistoryClick
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            FoodSearchSection(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (uiState.categories.isNotEmpty()) {
                FoodCategoriesSection(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected
                )
                Spacer(modifier = Modifier.height(AppSpacing.S))
            }

            if (uiState.restaurants.isNotEmpty()) {
                FoodSortSection(
                    selectedSort = uiState.selectedSort,
                    onSortSelected = viewModel::onSortSelected,
                    modifier = Modifier.padding(horizontal = Dimen.PaddingM)
                )
                Spacer(modifier = Modifier.height(AppSpacing.S))
            }

            FoodRestaurantContent(
                uiState = uiState,
                onRestaurantClick = onRestaurantClick,
                onRetry = { viewModel.loadRestaurants(uiState.searchQuery.ifBlank { null }) },
                onClearFilters = viewModel::onClearFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Shared Cart Bottom Bar when items exist
        if (uiState.totalCartQuantity > 0) {
            CartBottomBar(
                totalQuantity = uiState.totalCartQuantity,
                totalAmount = uiState.totalCartAmount,
                onCheckoutClick = {
                    uiState.cartRestaurantId?.let(onRestaurantClick)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun FoodRestaurantContent(
    uiState: FoodUiState,
    onRestaurantClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier) {
        when {
            uiState.isLoading && uiState.restaurants.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryColor
                )
            }
            uiState.errorMessage != null && uiState.restaurants.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(Dimen.PaddingL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.food_error_title),
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = uiState.errorMessage.asString(context),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.M))
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(AppShape.ShapeXL),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text(
                            text = stringResource(id = R.string.food_error_retry),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextWhite
                        )
                    }
                }
            }
            uiState.displayedRestaurants.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(Dimen.PaddingL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.food_empty_title),
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.food_empty_message),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (uiState.isFiltered) {
                        Spacer(modifier = Modifier.height(AppSpacing.M))
                        OutlinedButton(
                            onClick = onClearFilters,
                            shape = RoundedCornerShape(AppShape.ShapeXL)
                        ) {
                            Text(
                                text = stringResource(id = R.string.food_empty_clear_filters),
                                style = MaterialTheme.typography.s14.medium(),
                                color = PrimaryColor
                            )
                        }
                    }
                }
            }
            else -> {
                val bottomPadding = if (uiState.totalCartQuantity > 0) {
                    Dimen.SizeMega + Dimen.PaddingML
                } else {
                    Dimen.PaddingM
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = Dimen.PaddingXS,
                        bottom = bottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
                ) {
                    // Featured section (full-width edge-to-edge scroll)
                    if (uiState.showFeaturedSection) {
                        item {
                            FeaturedRestaurantsSection(
                                featuredRestaurants = uiState.featuredRestaurants,
                                distancesKm = uiState.distancesKm,
                                onRestaurantClick = onRestaurantClick
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.S))
                        }
                    }

                    // Section header: All restaurants + count
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimen.PaddingM),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.food_section_all_restaurants),
                                style = MaterialTheme.typography.s16.bold(),
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.food_restaurant_count,
                                    uiState.displayedRestaurants.size
                                ),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary
                            )
                        }
                    }

                    items(uiState.displayedRestaurants, key = { it.id }) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            distanceKm = uiState.distancesKm[restaurant.id],
                            onRestaurantClick = onRestaurantClick,
                            modifier = Modifier.padding(horizontal = Dimen.PaddingM)
                        )
                    }
                }
            }
        }
    }
}
