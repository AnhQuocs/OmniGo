package com.example.omnigo.features.customer.food.presentation.ui.detail

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.core.components.AppTextField
import com.example.omnigo.features.customer.food.presentation.ui.components.CartBottomBar
import com.example.omnigo.features.customer.food.presentation.viewmodel.RestaurantDetailViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
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
import com.example.omnigo.utils.s16

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
            uiState.isRestaurantLoading && uiState.restaurant == null -> {
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
                val bottomPadding = if (uiState.totalCartQuantity > 0) {
                    Dimen.SizeMega + Dimen.PaddingML
                } else {
                    Dimen.PaddingM
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomPadding)
                ) {
                    // Header Section
                    item {
                        RestaurantDetailHeaderSection(
                            restaurant = restaurant,
                            distanceKm = uiState.distanceKm,
                            onBackClick = onBackClick
                        )
                    }

                    // Menu Search Bar
                    item {
                        AppTextField(
                            value = uiState.menuSearchQuery,
                            onValueChange = viewModel::onMenuSearchQueryChanged,
                            placeholder = stringResource(id = R.string.restaurant_menu_search_hint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
                        )
                    }

                    // Category Filter Chips
                    if (uiState.categories.isNotEmpty() && uiState.menuSearchQuery.isBlank()) {
                        item {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimen.PaddingXS),
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
                                                style = if (isAllSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.medium()
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryContainer,
                                            selectedLabelColor = PrimaryColor,
                                            containerColor = SurfaceLight,
                                            labelColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(AppShape.ShapeXL)
                                    )
                                }

                                items(uiState.categories, key = { it }) { category ->
                                    val isSelected = uiState.selectedCategory == category
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onCategorySelected(category) },
                                        label = {
                                            Text(
                                                text = category,
                                                style = if (isSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.medium()
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryContainer,
                                            selectedLabelColor = PrimaryColor,
                                            containerColor = SurfaceLight,
                                            labelColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(AppShape.ShapeXL)
                                    )
                                }
                            }
                        }
                    }

                    // Menu Items Content
                    if (uiState.isMenuLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimen.PaddingXXL),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryColor)
                            }
                        }
                    } else if (uiState.totalItemCount == 0) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimen.PaddingXL),
                                contentAlignment = Alignment.Center
                            ) {
                                val emptyText = if (uiState.menuSearchQuery.isNotBlank()) {
                                    stringResource(id = R.string.restaurant_menu_search_empty, uiState.menuSearchQuery)
                                } else {
                                    stringResource(id = R.string.restaurant_detail_empty_menu)
                                }
                                Text(
                                    text = emptyText,
                                    style = MaterialTheme.typography.s14.normal(),
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Display sections with section title
                        uiState.displayedSections.forEach { section ->
                            item(key = "section_${section.category}") {
                                val title = if (section.isUncategorized) {
                                    stringResource(id = R.string.restaurant_menu_uncategorized)
                                } else {
                                    section.category
                                }
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.s16.bold(),
                                    color = TextPrimary,
                                    modifier = Modifier.padding(
                                        start = Dimen.PaddingM,
                                        end = Dimen.PaddingM,
                                        top = Dimen.PaddingM,
                                        bottom = Dimen.PaddingXS
                                    )
                                )
                            }

                            items(section.items, key = { it.id }) { item ->
                                val cartItem = uiState.cartItems[item.id]
                                val quantity = cartItem?.quantity ?: 0

                                MenuItemCard(
                                    menuItem = item,
                                    quantityInCart = quantity,
                                    canOrder = uiState.canOrder,
                                    onItemClick = viewModel::onMenuItemClick,
                                    onAddToCart = viewModel::onAddToCart,
                                    onRemoveFromCart = viewModel::onRemoveFromCart,
                                    modifier = Modifier.padding(
                                        horizontal = Dimen.PaddingM,
                                        vertical = AppSpacing.XSPlus
                                    )
                                )
                            }
                        }

                        // Reviews Section
                        item {
                            RestaurantReviewsSection(
                                reviews = uiState.reviews,
                                isLoading = uiState.isReviewsLoading,
                                selectedRating = uiState.selectedRatingFilter,
                                onRatingFilterSelected = { rating ->
                                    restaurant.id.let { resId ->
                                        viewModel.loadReviews(resId, rating)
                                    }
                                },
                                modifier = Modifier.padding(
                                    horizontal = Dimen.PaddingM,
                                    vertical = AppSpacing.L
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

        // Dish Detail Modal BottomSheet
        val detailItem = uiState.selectedMenuItemForDetail
        if (detailItem != null) {
            MenuItemDetailBottomSheet(
                menuItem = detailItem,
                quantity = uiState.itemDetailQuantity,
                note = uiState.itemDetailNote,
                canOrder = uiState.canOrder,
                onQuantityChange = viewModel::onDetailQuantityChanged,
                onNoteChange = viewModel::onDetailNoteChanged,
                onAddToCart = viewModel::onConfirmAddFromDetail,
                onDismiss = viewModel::onDismissMenuItemDetail
            )
        }

        // Restaurant Conflict Dialog
        val conflict = uiState.pendingConflict
        if (conflict != null) {
            RestaurantConflictDialog(
                currentRestaurantName = conflict.currentRestaurantName,
                newRestaurantName = conflict.newRestaurantName,
                onConfirm = viewModel::onConfirmReplaceCart,
                onDismiss = viewModel::onDismissConflictDialog
            )
        }
    }
}
