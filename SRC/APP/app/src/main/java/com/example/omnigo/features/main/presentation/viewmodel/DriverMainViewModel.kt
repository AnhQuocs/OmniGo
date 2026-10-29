package com.example.omnigo.features.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.driver.food.domain.usecase.GetDriverActiveFoodOrdersUseCase
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Exposes whether the driver currently has an unfinished ride or food delivery,
 * used to render the badge on the "Trips" bottom-bar tab.
 */
@HiltViewModel
class DriverMainViewModel @Inject constructor(
    private val driverTripSessionManager: DriverTripSessionManager,
    private val getDriverActiveFoodOrdersUseCase: GetDriverActiveFoodOrdersUseCase
) : ViewModel() {

    private val hasActiveFoodOrder = MutableStateFlow(false)

    val hasActiveTask: StateFlow<Boolean> = combine(
        driverTripSessionManager.activeBooking,
        hasActiveFoodOrder
    ) { booking, hasFood -> booking != null || hasFood }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    init {
        refreshActiveFoodOrders()
    }

    fun refreshActiveFoodOrders() {
        viewModelScope.launch {
            getDriverActiveFoodOrdersUseCase().onSuccess { orders ->
                hasActiveFoodOrder.value = orders.any { it.status.isActive }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

