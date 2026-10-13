package com.example.omnigo.features.customer.food.presentation.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.usecase.GetMyFoodOrdersUseCase
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
    private val getMyFoodOrdersUseCase: GetMyFoodOrdersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodOrderHistoryUiState())
    val uiState: StateFlow<FoodOrderHistoryUiState> = _uiState.asStateFlow()

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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            orders = result.orders,
                            errorMessage = null,
                            canRetry = true
                        )
                    }
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

    fun onFilterSelected(filter: HistoryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onRetry() {
        loadOrders()
    }

    fun onClearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
