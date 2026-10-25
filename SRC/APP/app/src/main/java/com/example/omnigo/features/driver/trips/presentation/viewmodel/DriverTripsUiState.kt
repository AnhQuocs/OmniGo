package com.example.omnigo.features.driver.trips.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.domain.model.DriverDailyReport

data class DriverTripsUiState(
    val isReportLoading: Boolean = false,
    val dailyReport: DriverDailyReport? = null,
    val reportErrorMessage: String? = null,
    val activeBooking: Booking? = null
)
