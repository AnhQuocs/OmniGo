package com.example.omnigo.features.driver.food.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.driver.common.presentation.DriverNavigationCoordinator
import com.example.omnigo.features.driver.common.presentation.DriverNavigationUiState
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.features.driver.food.domain.usecase.GetDriverFoodOrderDetailUseCase
import com.example.omnigo.features.driver.food.domain.usecase.UpdateDriverFoodOrderStatusUseCase
import com.example.omnigo.features.driver.food.domain.util.GeofenceCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverActiveDeliveryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDriverFoodOrderDetailUseCase: GetDriverFoodOrderDetailUseCase,
    private val updateDriverFoodOrderStatusUseCase: UpdateDriverFoodOrderStatusUseCase,
    private val locationTracker: LocationTracker,
    private val navigationCoordinator: DriverNavigationCoordinator
) : ViewModel() {

    private val navRestaurantLat: Double? = savedStateHandle.get<String>("restaurantLat")?.toDoubleOrNull()
    private val navRestaurantLng: Double? = savedStateHandle.get<String>("restaurantLng")?.toDoubleOrNull()

    private val _uiState = MutableStateFlow(DriverActiveDeliveryUiState())
    val uiState: StateFlow<DriverActiveDeliveryUiState> = _uiState.asStateFlow()

    val navigationState: StateFlow<DriverNavigationUiState> = navigationCoordinator.navigationState

    private var locationPollingJob: Job? = null
    private var liveLocationJob: Job? = null

    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getDriverFoodOrderDetailUseCase(orderId)
            result.onSuccess { order ->
                val effectiveOrder = if (order.restaurantLatitude == null && navRestaurantLat != null) {
                    order.copy(
                        restaurantLatitude = navRestaurantLat,
                        restaurantLongitude = navRestaurantLng
                    )
                } else {
                    order
                }
                _uiState.update {
                    it.copy(
                        order = effectiveOrder,
                        isLoading = false,
                        isCompleted = effectiveOrder.status == DriverDeliveryStatus.COMPLETED,
                        isCancelled = effectiveOrder.status == DriverDeliveryStatus.CANCELLED
                    )
                }
                syncNavigationDestination(effectiveOrder)
                refreshLocation()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load order detail"
                    )
                }
            }
        }
    }

    private fun syncNavigationDestination(order: DriverDeliveryOrder?) {
        viewModelScope.launch {
            if (order == null) {
                navigationCoordinator.updateDestination(null, null, "")
                return@launch
            }
            when (order.status) {
                DriverDeliveryStatus.ACCEPTED -> {
                    navigationCoordinator.updateDestination(
                        targetLat = order.restaurantLatitude,
                        targetLng = order.restaurantLongitude,
                        targetAddress = order.restaurantAddress.ifBlank { order.restaurantName },
                        vehicleType = "BIKE",
                        secondaryLat = order.dropOffLatitude,
                        secondaryLng = order.dropOffLongitude
                    )
                }
                DriverDeliveryStatus.DELIVERING -> {
                    navigationCoordinator.updateDestination(
                        targetLat = order.dropOffLatitude,
                        targetLng = order.dropOffLongitude,
                        targetAddress = order.dropOffAddress.ifBlank { order.customerName },
                        vehicleType = "BIKE",
                        secondaryLat = null,
                        secondaryLng = null
                    )
                }
                else -> {
                    navigationCoordinator.updateDestination(null, null, "")
                }
            }
        }
    }

    fun startPeriodicLocationTracking() {
        liveLocationJob?.cancel()
        liveLocationJob = viewModelScope.launch {
            navigationCoordinator.observeLocationUpdates().collect { update ->
                updateLocationAndDistance(update.latitude, update.longitude, update.bearing)
            }
        }

        locationPollingJob?.cancel()
        locationPollingJob = viewModelScope.launch {
            while (isActive) {
                refreshLocationInternal()
                delay(LOCATION_POLL_INTERVAL_MS)
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch { refreshLocationInternal() }
    }

    private suspend fun refreshLocationInternal() {
        if (!locationTracker.isLocationPermissionGranted() || !locationTracker.isGpsEnabled()) {
            _uiState.update { it.copy(distanceToTargetMeters = null, isWithinGeofence = false) }
            return
        }

        val location = locationTracker.getCurrentLocation()
        if (location != null) {
            updateLocationAndDistance(location.latitude, location.longitude)
        } else {
            _uiState.update { it.copy(distanceToTargetMeters = null, isWithinGeofence = false) }
        }
    }

    fun updateLocationAndDistance(lat: Double, lng: Double, bearing: Float? = null) {
        val currentOrder = _uiState.value.order
        val (targetLat, targetLng) = getTargetCoordinates(currentOrder)

        val distance = if (targetLat != null && targetLng != null) {
            GeofenceCalculator.calculateDistanceMeters(lat, lng, targetLat, targetLng)
        } else {
            null
        }

        val withinGeofence = distance != null && distance <= GeofenceCalculator.DEFAULT_GEOFENCE_RADIUS_METERS

        _uiState.update {
            it.copy(
                currentLatitude = lat,
                currentLongitude = lng,
                distanceToTargetMeters = distance,
                isWithinGeofence = withinGeofence
            )
        }

        viewModelScope.launch {
            navigationCoordinator.onLocationTick(lat, lng, bearing)
        }
    }

    private fun getTargetCoordinates(order: DriverDeliveryOrder?): Pair<Double?, Double?> {
        if (order == null) return Pair(null, null)
        return when (order.status) {
            DriverDeliveryStatus.ACCEPTED -> Pair(order.restaurantLatitude, order.restaurantLongitude)
            DriverDeliveryStatus.DELIVERING -> Pair(order.dropOffLatitude, order.dropOffLongitude)
            else -> Pair(null, null)
        }
    }

    fun onConfirmPickup() {
        val order = _uiState.value.order ?: return
        if (_uiState.value.isUpdatingStatus) return

        _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }

        viewModelScope.launch {
            refreshLocationInternal()
            if (!_uiState.value.isWithinGeofence) {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        errorMessage = ERROR_GEOFENCE_OUTSIDE,
                        showGeofenceOverrideDialog = true
                    )
                }
                return@launch
            }
            executePickupStatusUpdate(order.orderId)
        }
    }

    private suspend fun executePickupStatusUpdate(orderId: Long) {
        _uiState.update {
            it.copy(isUpdatingStatus = true, errorMessage = null, showGeofenceOverrideDialog = false)
        }
        val result = updateDriverFoodOrderStatusUseCase(orderId, "DELIVERING")
        result.onSuccess { updatedOrder ->
            _uiState.update {
                it.copy(
                    order = updatedOrder,
                    isUpdatingStatus = false,
                    actionMessage = MESSAGE_PICKUP_SUCCESS
                )
            }
            syncNavigationDestination(updatedOrder)
            refreshLocationInternal()
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isUpdatingStatus = false,
                    errorMessage = error.message ?: "Failed to update pickup status"
                )
            }
        }
    }

    fun onConfirmDelivered() {
        val order = _uiState.value.order ?: return
        if (_uiState.value.isUpdatingStatus) return

        _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }

        viewModelScope.launch {
            refreshLocationInternal()
            if (!_uiState.value.isWithinGeofence) {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        errorMessage = ERROR_GEOFENCE_OUTSIDE,
                        showGeofenceOverrideDialog = true
                    )
                }
                return@launch
            }
            executeDeliveredStatusUpdate(order.orderId)
        }
    }

    private suspend fun executeDeliveredStatusUpdate(orderId: Long) {
        _uiState.update {
            it.copy(isUpdatingStatus = true, errorMessage = null, showGeofenceOverrideDialog = false)
        }
        val result = updateDriverFoodOrderStatusUseCase(orderId, "COMPLETED")
        result.onSuccess { updatedOrder ->
            _uiState.update {
                it.copy(
                    order = updatedOrder,
                    isUpdatingStatus = false,
                    isCompleted = true,
                    actionMessage = MESSAGE_DELIVERY_SUCCESS
                )
            }
            syncNavigationDestination(updatedOrder)
            stopPeriodicLocationTracking()
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isUpdatingStatus = false,
                    errorMessage = error.message ?: "Failed to complete delivery"
                )
            }
        }
    }

    fun onConfirmGeofenceOverride() {
        val order = _uiState.value.order ?: return
        if (_uiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            when (order.status) {
                DriverDeliveryStatus.ACCEPTED -> executePickupStatusUpdate(order.orderId)
                DriverDeliveryStatus.DELIVERING -> executeDeliveredStatusUpdate(order.orderId)
                else -> _uiState.update { it.copy(showGeofenceOverrideDialog = false) }
            }
        }
    }

    fun dismissGeofenceOverrideDialog() {
        _uiState.update { it.copy(showGeofenceOverrideDialog = false) }
    }

    fun stopPeriodicLocationTracking() {
        locationPollingJob?.cancel()
        locationPollingJob = null
        liveLocationJob?.cancel()
        liveLocationJob = null
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicLocationTracking()
    }

    companion object {
        const val LOCATION_POLL_INTERVAL_MS = 4000L
        const val ERROR_GEOFENCE_OUTSIDE = "ERROR_GEOFENCE_OUTSIDE"
        const val MESSAGE_PICKUP_SUCCESS = "MESSAGE_PICKUP_SUCCESS"
        const val MESSAGE_DELIVERY_SUCCESS = "MESSAGE_DELIVERY_SUCCESS"
    }
}
