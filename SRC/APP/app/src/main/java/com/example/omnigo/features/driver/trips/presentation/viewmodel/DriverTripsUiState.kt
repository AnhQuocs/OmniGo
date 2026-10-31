package com.example.omnigo.features.driver.trips.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.trips.domain.model.DriverHistoryDayGroup
import com.example.omnigo.features.driver.trips.domain.model.DriverTripFilter

data class DriverTripsUiState(
    val isReportLoading: Boolean = false,
    val dailyReport: DriverDailyReport? = null,
    val reportErrorMessage: String? = null,
    val activeBooking: Booking? = null,
    val activeFoodOrders: List<DriverDeliveryOrder> = emptyList(),
    val historyFoodOrders: List<DriverDeliveryOrder> = emptyList(),
    val historyBookings: List<Booking> = emptyList(),
    val selectedFilter: DriverTripFilter = DriverTripFilter.ALL,
    val groupedHistory: List<DriverHistoryDayGroup> = emptyList()
)
