package com.example.omnigo.features.customer.food.presentation.viewmodel.history

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.utils.UiText

data class FoodOrderHistoryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val orders: List<FoodOrder> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val errorMessage: UiText? = null,
    val canRetry: Boolean = false
)

enum class HistoryFilter {
    ALL,
    ACTIVE,
    COMPLETED
}
