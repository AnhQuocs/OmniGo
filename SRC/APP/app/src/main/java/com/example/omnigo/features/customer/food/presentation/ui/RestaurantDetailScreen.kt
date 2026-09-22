package com.example.omnigo.features.customer.food.presentation.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.ui.components.CartBottomBar
import com.example.omnigo.features.customer.food.presentation.ui.components.MenuItemCard
import com.example.omnigo.features.customer.food.presentation.ui.components.RestaurantDetailHeaderSection
import com.example.omnigo.features.customer.food.presentation.viewmodel.RestaurantDetailViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15

@Composable
fun RestaurantDetailScreen(
    restaurantId: Long,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RestaurantDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(restaurantId) {
        viewModel.loadRestaurant(restaurantId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        when {
            uiState.isLoading && uiState.restaurant == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryColor
                )
            }
            uiState.errorMessage != null && uiState.restaurant == null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(Dimen.PaddingL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.restaurant_detail_error),
                        style = MaterialTheme.typography.s15.medium(),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.M))
                    Button(
                        onClick = { viewModel.loadRestaurant(restaurantId) },
                        shape = RoundedCornerShape(AppShape.ShapeXL),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text(
                            text = stringResource(id = R.string.restaurant_detail_retry),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextWhite
                        )
                    }
                }
            }
            uiState.restaurant != null -> {
                val restaurant = uiState.restaurant!!
                val displayedItems = if (uiState.selectedCategory.isNullOrBlank()) {
                    restaurant.menuItems
                } else {
                    restaurant.menuItems.filter {
                        it.category.equals(uiState.selectedCategory, ignoreCase = true)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Header Section
                    item {
                        RestaurantDetailHeaderSection(
                            restaurant = restaurant,
                            distanceKm = uiState.distanceKm,
                            estimatedMinutes = uiState.estimatedDeliveryMinutes,
                            onBackClick = onBackClick
                        )
                    }

                    // Category Filter Chips
                    if (uiState.categories.isNotEmpty()) {
                        item {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundLight)
                                    .padding(vertical = Dimen.PaddingS),
                                contentPadding = PaddingValues(horizontal = Dimen.PaddingM),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                            ) {
                                item {
                                    val isAllSelected = uiState.selectedCategory == null
                                    FilterChip(
                                        selected = isAllSelected,
                                        onClick = { viewModel.onCategorySelected(null) },
                                        label = {
                                            Text(
                                                text = stringResource(id = R.string.restaurant_detail_category_all),
                                                style = if (isAllSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.normal()
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryColor,
                                            selectedLabelColor = TextWhite,
                                            containerColor = SurfaceLight,
                                            labelColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(AppShape.ShapeXL)
                                    )
                                }

                                items(uiState.categories) { category ->
                                    val isSelected = uiState.selectedCategory == category
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onCategorySelected(category) },
                                        label = {
                                            Text(
                                                text = category,
                                                style = if (isSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.normal()
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryColor,
                                            selectedLabelColor = TextWhite,
                                            containerColor = SurfaceLight,
                                            labelColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(AppShape.ShapeXL)
                                    )
                                }
                            }
                        }
                    }

                    // Menu Items List
                    if (displayedItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Dimen.PaddingXL),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.restaurant_detail_empty_menu),
                                    style = MaterialTheme.typography.s14.normal(),
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(displayedItems, key = { it.id }) { item ->
                            val cartItem = uiState.cartItems[item.id]
                            val quantity = cartItem?.quantity ?: 0

                            MenuItemCard(
                                menuItem = item,
                                quantityInCart = quantity,
                                onAddToCart = viewModel::onAddToCart,
                                onRemoveFromCart = viewModel::onRemoveFromCart,
                                modifier = Modifier.padding(
                                    horizontal = Dimen.PaddingM,
                                    vertical = AppSpacing.XSPlus
                                )
                            )
                        }
                    }
                }

                // Sticky Bottom Cart Bar
                CartBottomBar(
                    totalQuantity = uiState.totalCartQuantity,
                    totalAmount = uiState.totalCartAmount,
                    onCheckoutClick = onCheckoutClick,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
