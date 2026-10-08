package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.DriverProfile
import com.example.omnigo.utils.UiText

data class FoodOrderDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val order: FoodOrder? = null,
    val driverProfile: DriverProfile? = null,
    val isDriverProfileLoading: Boolean = false,
    val driverProfileError: UiText? = null,
    val errorMessage: UiText? = null,
    val canRetry: Boolean = true,
    val isActionLoading: Boolean = false,
    val actionMessage: UiText? = null,
    val isCancelDialogOpen: Boolean = false,
    val retryDriverCount: Int = 0
)
