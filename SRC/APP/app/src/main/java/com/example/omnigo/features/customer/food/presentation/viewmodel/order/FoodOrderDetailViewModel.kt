package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.CancelFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult
import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.model.RetryDriverResult
import com.example.omnigo.features.customer.food.domain.model.SwitchToCashResult
import com.example.omnigo.features.customer.food.domain.usecase.CancelFoodOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodOrderDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.RetryDriverUseCase
import com.example.omnigo.features.customer.food.domain.usecase.SwitchToCashUseCase
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodOrderDetailViewModel @Inject constructor(
    private val getFoodOrderDetailUseCase: GetFoodOrderDetailUseCase,
    private val getDriverProfileUseCase: GetDriverProfileUseCase,
    private val cancelFoodOrderUseCase: CancelFoodOrderUseCase,
    private val switchToCashUseCase: SwitchToCashUseCase,
    private val retryDriverUseCase: RetryDriverUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L

    private val _uiState = MutableStateFlow(FoodOrderDetailUiState())
    val uiState: StateFlow<FoodOrderDetailUiState> = _uiState.asStateFlow()
    private var driverProfileJob: Job? = null

    init {
        loadOrderDetail(orderId)
    }

    fun loadOrderDetail(id: Long = orderId, isRefresh: Boolean = false) {
        if (id <= 0) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = UiText.StringResource(R.string.order_detail_invalid_order_id),
                    canRetry = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) {
                    it.copy(
                        isLoading = it.order == null,
                        isRefreshing = true,
                        errorMessage = null
                    )
                } else {
                    it.copy(isLoading = true, isRefreshing = false, errorMessage = null)
                }
            }

            when (val result = getFoodOrderDetailUseCase(id)) {
                is GetFoodOrderDetailResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            order = result.order,
                            driverProfile = if (it.order?.driverId == result.order.driverId) {
                                it.driverProfile
                            } else {
                                null
                            },
                            errorMessage = null,
                            canRetry = true
                        )
                    }
                    loadDriverProfile(result.order.driverId)
                }
                is GetFoodOrderDetailResult.Error -> {
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

    private fun loadDriverProfile(driverId: Long?) {
        driverProfileJob?.cancel()
        if (driverId == null || driverId <= 0) {
            _uiState.update {
                it.copy(
                    driverProfile = null,
                    isDriverProfileLoading = false,
                    driverProfileError = null
                )
            }
            return
        }

        _uiState.update { it.copy(isDriverProfileLoading = true, driverProfileError = null) }
        driverProfileJob = viewModelScope.launch {
            when (val result = getDriverProfileUseCase(driverId)) {
                is GetDriverProfileResult.Success -> {
                    _uiState.update {
                        if (it.order?.driverId == driverId) {
                            it.copy(
                                driverProfile = result.profile,
                                isDriverProfileLoading = false,
                                driverProfileError = null
                            )
                        } else {
                            it
                        }
                    }
                }
                is GetDriverProfileResult.Error -> {
                    _uiState.update {
                        if (it.order?.driverId == driverId) {
                            it.copy(
                                isDriverProfileLoading = false,
                                driverProfileError = result.message
                                    ?.takeIf(String::isNotBlank)
                                    ?.let(UiText::DynamicString)
                                    ?: result.error.asUiText()
                            )
                        } else {
                            it
                        }
                    }
                }
            }
        }
    }

    fun onOpenCancelDialog() {
        _uiState.update { it.copy(isCancelDialogOpen = true) }
    }

    fun onDismissCancelDialog() {
        _uiState.update { it.copy(isCancelDialogOpen = false) }
    }

    fun onCancelOrder(reasonCode: String?, reason: String?) {
        if (orderId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, isCancelDialogOpen = false) }
            when (val result = cancelFoodOrderUseCase(orderId, reasonCode, reason)) {
                is CancelFoodOrderResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            order = result.order,
                            actionMessage = UiText.StringResource(R.string.order_cancel_success)
                        )
                    }
                }
                is CancelFoodOrderResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            actionMessage = result.message
                                ?.takeIf(String::isNotBlank)
                                ?.let(UiText::DynamicString)
                                ?: result.error.asUiText()
                        )
                    }
                }
            }
        }
    }

    fun onSwitchToCash() {
        if (orderId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            when (val result = switchToCashUseCase(orderId)) {
                is SwitchToCashResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            order = result.order,
                            actionMessage = UiText.StringResource(R.string.order_switch_cash_success)
                        )
                    }
                }
                is SwitchToCashResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            actionMessage = result.message
                                ?.takeIf(String::isNotBlank)
                                ?.let(UiText::DynamicString)
                                ?: result.error.asUiText()
                        )
                    }
                }
            }
        }
    }

    fun onRetryDriver() {
        if (orderId <= 0) return
        if (_uiState.value.retryDriverCount >= 3) {
            _uiState.update {
                it.copy(actionMessage = UiText.StringResource(R.string.order_detail_retry_driver_max_reached))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            when (val result = retryDriverUseCase(orderId)) {
                is RetryDriverResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            order = result.order,
                            retryDriverCount = it.retryDriverCount + 1,
                            actionMessage = UiText.StringResource(R.string.order_retry_driver_success)
                        )
                    }
                }
                is RetryDriverResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            actionMessage = result.message
                                ?.takeIf(String::isNotBlank)
                                ?.let(UiText::DynamicString)
                                ?: result.error.asUiText()
                        )
                    }
                }
            }
        }
    }

    fun onClearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    fun onRetry() {
        loadOrderDetail(orderId)
    }

    fun onClearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
