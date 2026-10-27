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
import com.example.omnigo.features.customer.food.domain.repository.ActiveFoodOrderTrackingRepository
import com.example.omnigo.features.customer.food.domain.usecase.CancelFoodOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodOrderDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.RetryDriverUseCase
import com.example.omnigo.features.customer.food.domain.usecase.SwitchToCashUseCase
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantItemsUseCase
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
    private val activeTrackingRepository: ActiveFoodOrderTrackingRepository,
    savedStateHandle: SavedStateHandle,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val getRestaurantItemsUseCase: GetRestaurantItemsUseCase
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L

    private val _uiState = MutableStateFlow(FoodOrderDetailUiState())
    val uiState: StateFlow<FoodOrderDetailUiState> = _uiState.asStateFlow()
    private var driverProfileJob: Job? = null
    private var trackingStateJob: Job? = null

    init {
        loadOrderDetail(orderId)
        if (orderId > 0) {
            observeRealtimeTracking(orderId)
        }
    }

    private fun observeRealtimeTracking(id: Long) {
        if (id <= 0) return
        trackingStateJob?.cancel()
        viewModelScope.launch {
            activeTrackingRepository.activate(id)
        }
        trackingStateJob = viewModelScope.launch {
            activeTrackingRepository.activeTrackingState.collect { trackingState ->
                if (trackingState != null && trackingState.orderId == id) {
                    _uiState.update { current ->
                        val updatedOrder = current.order?.let { order ->
                            var modified = order
                            if (!trackingState.status.isNullOrBlank() && trackingState.status != modified.status) {
                                modified = modified.copy(status = trackingState.status)
                            }
                            val profile = trackingState.driverProfile
                            if (profile != null && modified.driverId != profile.driverId) {
                                modified = modified.copy(
                                    driverId = profile.driverId,
                                    driverName = profile.driverName ?: modified.driverName,
                                    driverPhone = profile.driverPhone ?: modified.driverPhone,
                                    driverVehiclePlate = profile.vehiclePlate ?: modified.driverVehiclePlate
                                )
                            }
                            modified
                        }

                        val updatedProfile = trackingState.driverProfile ?: current.driverProfile
                        val updatedLocation = trackingState.driverLocation ?: current.driverLocation

                        current.copy(
                            order = updatedOrder,
                            driverProfile = updatedProfile,
                            driverLocation = updatedLocation
                        )
                    }
                }
            }
        }
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
                    enrichOrderWithRestaurantData(result.order)
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

    private fun enrichOrderWithRestaurantData(order: FoodOrder) {
        val restaurantId = order.restaurantId
        if (restaurantId <= 0) return

        viewModelScope.launch {
            var restaurantImageUrl: String? = null
            var restaurantAddress: String? = null
            var catalogItems: List<MenuItem> = emptyList()

            when (val res = getRestaurantDetailUseCase(restaurantId)) {
                is GetRestaurantDetailResult.Success -> {
                    restaurantImageUrl = res.restaurant.imageUrl.takeIf { it.isNotBlank() }
                    restaurantAddress = res.restaurant.address.takeIf { it.isNotBlank() }
                    catalogItems = res.restaurant.menuItems
                }
                else -> Unit
            }

            if (catalogItems.isEmpty() || catalogItems.none { it.imageUrl.isNotBlank() }) {
                when (val menuRes = getRestaurantItemsUseCase(restaurantId, availableOnly = false)) {
                    is GetMenuItemsResult.Success -> catalogItems = menuRes.items
                    else -> Unit
                }
            }

            _uiState.update { current ->
                val curOrder = current.order ?: return@update current
                if (curOrder.id != order.id) return@update current

                val enrichedItems = curOrder.items.map { item ->
                    val match = catalogItems.firstOrNull {
                        it.id == item.menuItemId || it.name.trim().equals(item.itemName.trim(), ignoreCase = true)
                    }
                    val realImage = item.imageUrl?.takeIf { it.isNotBlank() } ?: match?.imageUrl
                    if (!realImage.isNullOrBlank() && realImage != item.imageUrl) {
                        item.copy(imageUrl = realImage)
                    } else {
                        item
                    }
                }

                val finalImage = curOrder.restaurantImageUrl?.takeIf { it.isNotBlank() } ?: restaurantImageUrl
                val finalAddress = curOrder.restaurantAddress?.takeIf { it.isNotBlank() } ?: restaurantAddress

                current.copy(
                    order = curOrder.copy(
                        restaurantImageUrl = finalImage,
                        restaurantAddress = finalAddress,
                        items = enrichedItems
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingStateJob?.cancel()
        driverProfileJob?.cancel()
        viewModelScope.launch {
            activeTrackingRepository.clear()
        }
    }
}
