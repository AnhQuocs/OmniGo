package com.example.omnigo.features.driver.food.presentation.viewmodel

import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder

data class DriverActiveDeliveryUiState(
    val order: DriverDeliveryOrder? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val distanceToTargetMeters: Double? = null,
    val isWithinGeofence: Boolean = false,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val showGeofenceOverrideDialog: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false
)
