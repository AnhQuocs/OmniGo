package com.example.omnigo.features.customer.food.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.ui.components.FoodCategoriesSection
import com.example.omnigo.features.customer.food.presentation.ui.components.FoodHeaderSection
import com.example.omnigo.features.customer.food.presentation.ui.components.FoodSearchSection
import com.example.omnigo.features.customer.food.presentation.ui.components.RestaurantCard
import com.example.omnigo.features.customer.food.presentation.viewmodel.FoodUiState
import com.example.omnigo.features.customer.food.presentation.viewmodel.FoodViewModel
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14

@Composable
fun FoodHomeScreen(
    onBackClick: () -> Unit,
    onRestaurantClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        FoodHeaderSection(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(AppSpacing.S))

        FoodSearchSection(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        FoodCategoriesSection(
            selectedCategoryId = uiState.selectedCategory,
            onCategorySelected = viewModel::onCategorySelected
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        FoodRestaurantContent(
            uiState = uiState,
            onRestaurantClick = onRestaurantClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun FoodRestaurantContent(
    uiState: FoodUiState,
    onRestaurantClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (uiState.isLoading && uiState.restaurants.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PrimaryColor
            )
        } else if (uiState.errorMessage != null && uiState.restaurants.isEmpty()) {
            Text(
                text = uiState.errorMessage.asString(),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(Dimen.PaddingM)
            )
        } else if (uiState.restaurants.isEmpty()) {
            Text(
                text = stringResource(id = R.string.food_empty_title),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val selectedCat = uiState.selectedCategory
            val filteredList = if (selectedCat.isNullOrEmpty()) {
                uiState.restaurants
            } else {
                uiState.restaurants.filter { restaurant ->
                    restaurant.menuItems.any { item ->
                        item.category.lowercase().contains(selectedCat)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = Dimen.PaddingM,
                    vertical = Dimen.PaddingS
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                items(filteredList) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onRestaurantClick = onRestaurantClick
                    )
                }
            }
        }
    }
}
