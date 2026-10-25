package com.example.omnigo.features.customer.activity.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.Booking

data class ActivityUiState(
    val isLoading: Boolean = false,
    val ongoingBookings: List<Booking> = emptyList(),
    val historyBookings: List<Booking> = emptyList(),
    val errorMessage: String? = null
) {
    val hasOngoing: Boolean
        get() = ongoingBookings.isNotEmpty()

    val hasHistory: Boolean
        get() = historyBookings.isNotEmpty()
}
