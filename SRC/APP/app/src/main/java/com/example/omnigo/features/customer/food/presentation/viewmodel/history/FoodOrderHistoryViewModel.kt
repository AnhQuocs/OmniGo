package com.example.omnigo.features.customer.food.presentation.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetMyFoodOrdersUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantItemsUseCase
import com.example.omnigo.features.customer.food.domain.util.FoodOrderHistoryHelper
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodOrderHistoryViewModel @Inject constructor(
    private val getMyFoodOrdersUseCase: GetMyFoodOrdersUseCase,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val getRestaurantItemsUseCase: GetRestaurantItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodOrderHistoryUiState())
    val uiState: StateFlow<FoodOrderHistoryUiState> = _uiState.asStateFlow()

    private val restaurantCache = mutableMapOf<Long, Restaurant>()
    private val menuCatalogCache = mutableMapOf<Long, List<MenuItem>>()

    init {
        loadOrders()
    }

    fun loadOrders(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) {
                    it.copy(
                        isLoading = it.orders.isEmpty(),
                        isRefreshing = true,
                        errorMessage = null
                    )
                } else {
                    it.copy(
                        isLoading = true,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }

            when (val result = getMyFoodOrdersUseCase()) {
                is GetMyFoodOrdersResult.Success -> {
                    val initialOrders = applyCachedEnrichment(result.orders)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            orders = initialOrders,
                            errorMessage = null,
                            canRetry = true
                        )
                    }
                    enrichOrdersWithRestaurantData(initialOrders)
                }
                is GetMyFoodOrdersResult.Error -> {
                    val errorUiText = if (!result.message.isNullOrBlank()) {
                        UiText.DynamicString(result.message)
                    } else {
                        result.error.asUiText()
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = errorUiText,
                            canRetry = true
                        )
                    }
                }
            }
        }
    }

    fun syncOrdersSilently() {
        viewModelScope.launch {
            when (val result = getMyFoodOrdersUseCase()) {
                is GetMyFoodOrdersResult.Success -> {
                    val initialOrders = applyCachedEnrichment(result.orders)
                    _uiState.update {
                        it.copy(
                            orders = initialOrders,
                            errorMessage = null
                        )
                    }
                    enrichOrdersWithRestaurantData(initialOrders)
                }
                is GetMyFoodOrdersResult.Error -> {
                    // Silently ignore background sync errors to preserve current UI
                }
            }
        }
    }

    fun onFilterSelected(filter: HistoryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onRetry() {
        loadOrders()
    }

    fun onClearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun applyCachedEnrichment(orders: List<FoodOrder>): List<FoodOrder> {
        return orders.map { order ->
            val restaurant = restaurantCache[order.restaurantId]
            val catalog = menuCatalogCache[order.restaurantId].orEmpty()
            if (restaurant != null || catalog.isNotEmpty()) {
                FoodOrderHistoryHelper.enrichOrder(order, restaurant, catalog)
            } else {
                order
            }
        }
    }

    private fun enrichOrdersWithRestaurantData(orders: List<FoodOrder>) {
        val restaurantIdsToFetch = orders
            .map { it.restaurantId }
            .filter { it > 0L }
            .distinct()
            .filter { id ->
                !restaurantCache.containsKey(id) ||
                    orders.any { o ->
                        o.restaurantId == id &&
                            (o.restaurantImageUrl.isNullOrBlank() ||
                                o.restaurantAddress.isNullOrBlank() ||
                                o.items.any { it.imageUrl.isNullOrBlank() })
                    }
            }

        if (restaurantIdsToFetch.isEmpty()) return

        viewModelScope.launch {
            for (restaurantId in restaurantIdsToFetch) {
                if (!restaurantCache.containsKey(restaurantId)) {
                    when (val res = getRestaurantDetailUseCase(restaurantId)) {
                        is GetRestaurantDetailResult.Success -> {
                            restaurantCache[restaurantId] = res.restaurant
                            if (res.restaurant.menuItems.isNotEmpty()) {
                                menuCatalogCache[restaurantId] = res.restaurant.menuItems
                            }
                        }
                        else -> Unit
                    }
                }

                val currentCatalog = menuCatalogCache[restaurantId].orEmpty()
                if (currentCatalog.isEmpty() || currentCatalog.none { it.imageUrl.isNotBlank() }) {
                    when (val menuRes = getRestaurantItemsUseCase(restaurantId, availableOnly = false)) {
                        is GetMenuItemsResult.Success -> {
                            menuCatalogCache[restaurantId] = menuRes.items
                        }
                        else -> Unit
                    }
                }
            }

            _uiState.update { current ->
                current.copy(orders = applyCachedEnrichment(current.orders))
            }
        }
    }
}
