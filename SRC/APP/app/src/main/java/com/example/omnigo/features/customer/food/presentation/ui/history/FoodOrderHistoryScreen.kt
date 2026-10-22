package com.example.omnigo.features.customer.food.presentation.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.presentation.viewmodel.history.FoodOrderHistoryViewModel
import com.example.omnigo.features.customer.food.presentation.viewmodel.history.HistoryFilter
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
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodOrderHistoryScreen(
    onBackClick: () -> Unit,
    onOrderClick: (Long) -> Unit,
    onReorderClick: (FoodOrder) -> Unit = {},
    onReviewClick: (FoodOrder) -> Unit = {},
    onExploreClick: () -> Unit = onBackClick,
    modifier: Modifier = Modifier,
    viewModel: FoodOrderHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Silently sync orders whenever screen resumes (e.g. after returning from detail/cancel screen)
    // without showing annoying spinner
    LifecycleResumeEffect(Unit) {
        viewModel.syncOrdersSilently()
        onPauseOrDispose { }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.food_history_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.order_detail_action_back),
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadOrders(isRefresh = true) },
                        enabled = !state.isRefreshing && !state.isLoading
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeS),
                                color = PrimaryColor,
                                strokeWidth = Dimen.PaddingXXS
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.order_detail_action_refresh),
                                tint = PrimaryColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
        ) {
            FilterSection(
                selectedFilter = state.selectedFilter,
                allOrders = state.orders,
                onFilterSelected = viewModel::onFilterSelected
            )

            val filteredOrders = remember(state.orders, state.selectedFilter) {
                state.orders.filter { order ->
                    val upper = order.status.uppercase(Locale.ROOT)
                    when (state.selectedFilter) {
                        HistoryFilter.ALL -> true
                        HistoryFilter.ONGOING -> upper in listOf(
                            "PENDING", "ACCEPTED", "PREPARING", "DRIVER_SEARCHING",
                            "DRIVER_ASSIGNED", "PICKED_UP", "DELIVERING", "ARRIVED_CUSTOMER",
                            "AWAITING_PAYMENT", "NO_DRIVER_FOUND", "PENDING_MERCHANT"
                        )
                        HistoryFilter.DELIVERED -> upper in listOf("COMPLETED", "DELIVERED")
                        HistoryFilter.CANCELLED -> upper in listOf("CANCELLED", "REJECTED")
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isLoading && state.orders.isEmpty() -> {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                    state.errorMessage != null && state.orders.isEmpty() -> {
                        Column(
                            modifier = Modifier.padding(Dimen.PaddingXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.errorMessage?.asString(context)
                                    ?: stringResource(R.string.order_detail_error_title),
                                style = MaterialTheme.typography.s14.normal(),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))
                            Button(
                                onClick = viewModel::onRetry,
                                shape = RoundedCornerShape(AppShape.ShapeM),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Text(
                                    text = stringResource(R.string.order_detail_retry),
                                    color = TextWhite,
                                    style = MaterialTheme.typography.s14.bold()
                                )
                            }
                        }
                    }
                    filteredOrders.isEmpty() -> {
                        FoodOrderHistoryEmptyState(
                            onExploreClick = onExploreClick
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Dimen.PaddingML),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge)
                        ) {
                            items(
                                items = filteredOrders,
                                key = { it.id }
                            ) { order ->
                                FoodOrderHistoryCard(
                                    order = order,
                                    onClick = { onOrderClick(order.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedFilter: HistoryFilter,
    allOrders: List<FoodOrder>,
    onFilterSelected: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimen.PaddingML, vertical = Dimen.PaddingS),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
    ) {
        HistoryFilter.entries.forEach { filter ->
            val label = when (filter) {
                HistoryFilter.ALL -> stringResource(R.string.food_history_filter_all)
                HistoryFilter.ONGOING -> stringResource(R.string.food_history_filter_ongoing)
                HistoryFilter.DELIVERED -> stringResource(R.string.food_history_filter_delivered)
                HistoryFilter.CANCELLED -> stringResource(R.string.food_history_filter_cancelled)
            }
            val isSelected = selectedFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = label,
                        style = if (isSelected) MaterialTheme.typography.s14.bold() else MaterialTheme.typography.s14.normal()
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor.copy(alpha = 0.12f),
                    selectedLabelColor = PrimaryColor,
                    containerColor = BackgroundLight,
                    labelColor = TextSecondary
                ),
                border = null,
                shape = RoundedCornerShape(AppShape.ShapeXL2)
            )
        }
    }
}
