package com.example.omnigo.features.driver.ride.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.customer.ride.domain.model.ArriveRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CompleteRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.StartRideBookingResult
import com.example.omnigo.features.driver.domain.usecase.ArrivePickupUseCase
import com.example.omnigo.features.driver.domain.usecase.CompleteTripUseCase
import com.example.omnigo.features.driver.domain.usecase.RejectRideBookingUseCase
import com.example.omnigo.features.driver.domain.usecase.StartTripUseCase
import com.example.omnigo.features.driver.domain.util.RideGeofenceCalculator
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
import com.example.omnigo.features.driver.ride.presentation.state.DriverActiveTripUiState
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
class DriverActiveTripViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val arrivePickupUseCase: ArrivePickupUseCase,
    private val startTripUseCase: StartTripUseCase,
    private val completeTripUseCase: CompleteTripUseCase,
    private val rejectRideBookingUseCase: RejectRideBookingUseCase,
    private val locationTracker: LocationTracker,
    private val driverTripSessionManager: DriverTripSessionManager
) : ViewModel() {

    companion object {
        const val LOCATION_POLL_INTERVAL_MS = 3000L
        const val GEOFENCE_WARNING_PICKUP = "GEOFENCE_WARNING_PICKUP"
        const val GEOFENCE_WARNING_DROPOFF = "GEOFENCE_WARNING_DROPOFF"
    }

    private val navBookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L
    private val navPickupLat: Double? = savedStateHandle.get<String>("pickupLat")?.toDoubleOrNull()
    private val navPickupLng: Double? = savedStateHandle.get<String>("pickupLng")?.toDoubleOrNull()
    private val navDropoffLat: Double? = savedStateHandle.get<String>("dropoffLat")?.toDoubleOrNull()
    private val navDropoffLng: Double? = savedStateHandle.get<String>("dropoffLng")?.toDoubleOrNull()
    private val navPrice: Double? = savedStateHandle.get<String>("price")?.toDoubleOrNull()
    private val navPaymentMethod: String? = savedStateHandle.get<String>("paymentMethod")

    private val _uiState = MutableStateFlow(DriverActiveTripUiState(bookingId = navBookingId))
    val uiState: StateFlow<DriverActiveTripUiState> = _uiState.asStateFlow()

    private var locationPollingJob: Job? = null

    init {
        initializeTripData()
    }

    private fun initializeTripData() {
        val cachedBooking = driverTripSessionManager.activeBooking.value
        val effectiveBookingId = if (navBookingId > 0L) navBookingId else cachedBooking?.bookingId ?: 0L

        val startLat = cachedBooking?.startLatitude ?: navPickupLat
        val startLng = cachedBooking?.startLongitude ?: navPickupLng
        val endLat = cachedBooking?.endLatitude ?: navDropoffLat
        val endLng = cachedBooking?.endLongitude ?: navDropoffLng
        val price = cachedBooking?.price ?: navPrice ?: 0.0
        val paymentMethod = cachedBooking?.paymentMethod ?: navPaymentMethod ?: "CASH"
        val status = cachedBooking?.status ?: BookingStatus.ACCEPTED

        _uiState.update {
            it.copy(
                bookingId = effectiveBookingId,
                status = status,
                startLatitude = startLat,
                startLongitude = startLng,
                endLatitude = endLat,
                endLongitude = endLng,
                price = price,
                paymentMethod = paymentMethod,
                customerId = cachedBooking?.customerId,
                customerAvatarUrl = cachedBooking?.customerAvatarUrl
            )
        }

        resolveAddresses(startLat, startLng, endLat, endLng)
    }

    fun startPeriodicLocationTracking() {
        locationPollingJob?.cancel()
        locationPollingJob = viewModelScope.launch {
            while (isActive) {
                refreshLocationInternal()
                delay(LOCATION_POLL_INTERVAL_MS)
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            refreshLocationInternal()
        }
    }

    private suspend fun refreshLocationInternal() {
        if (!locationTracker.isLocationPermissionGranted() || !locationTracker.isGpsEnabled()) {
            _uiState.update {
                it.copy(
                    distanceToTargetMeters = null,
                    isWithinGeofence = false
                )
            }
            return
        }

        val location = locationTracker.getCurrentLocation()
        if (location != null) {
            updateLocationAndDistance(location.latitude, location.longitude)
        } else {
            _uiState.update {
                it.copy(
                    distanceToTargetMeters = null,
                    isWithinGeofence = false
                )
            }
        }
    }

    fun updateLocationAndDistance(lat: Double, lng: Double) {
        val currentState = _uiState.value
        val (targetLat, targetLng) = getTargetCoordinates(currentState)

        val distance = if (targetLat != null && targetLng != null) {
            RideGeofenceCalculator.calculateDistanceMeters(lat, lng, targetLat, targetLng)
        } else {
            null
        }

        val withinGeofence = if (distance != null) {
            distance <= RideGeofenceCalculator.DEFAULT_GEOFENCE_RADIUS_METERS
        } else {
            false
        }

        _uiState.update {
            it.copy(
                currentLatitude = lat,
                currentLongitude = lng,
                distanceToTargetMeters = distance,
                isWithinGeofence = withinGeofence
            )
        }
    }

    private fun getTargetCoordinates(state: DriverActiveTripUiState): Pair<Double?, Double?> {
        return when (state.status) {
            BookingStatus.ACCEPTED, BookingStatus.ARRIVED -> Pair(state.startLatitude, state.startLongitude)
            BookingStatus.IN_PROGRESS -> Pair(state.endLatitude, state.endLongitude)
            else -> Pair(null, null)
        }
    }

    private fun resolveAddresses(startLat: Double?, startLng: Double?, endLat: Double?, endLng: Double?) {
        viewModelScope.launch {
            if (startLat != null && startLng != null) {
                val pickup = locationTracker.getAddressFromCoordinates(startLat, startLng)
                if (pickup != null) {
                    _uiState.update { it.copy(pickupAddress = pickup) }
                }
            }
            if (endLat != null && endLng != null) {
                val dropoff = locationTracker.getAddressFromCoordinates(endLat, endLng)
                if (dropoff != null) {
                    _uiState.update { it.copy(dropoffAddress = dropoff) }
                }
            }
        }
    }

    fun onArrivePickup() {
        val bookingId = _uiState.value.bookingId
        if (bookingId <= 0L || _uiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            refreshLocationInternal()
            if (!_uiState.value.isWithinGeofence) {
                _uiState.update { it.copy(errorMessage = GEOFENCE_WARNING_PICKUP) }
                return@launch
            }

            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
            val result = arrivePickupUseCase(bookingId)
            when (result) {
                is ArriveRideBookingResult.Success -> {
                    updateBookingSuccess(result.booking, BookingStatus.ARRIVED)
                }
                is ArriveRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingStatus = false,
                            errorMessage = result.message ?: "Failed to update arrival status"
                        )
                    }
                }
            }
        }
    }

    fun onStartTrip() {
        val bookingId = _uiState.value.bookingId
        if (bookingId <= 0L || _uiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
            val result = startTripUseCase(bookingId)
            when (result) {
                is StartRideBookingResult.Success -> {
                    updateBookingSuccess(result.booking, BookingStatus.IN_PROGRESS)
                    refreshLocationInternal()
                }
                is StartRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingStatus = false,
                            errorMessage = result.message ?: "Failed to start trip"
                        )
                    }
                }
            }
        }
    }

    fun onCompleteTrip() {
        val currentState = _uiState.value
        val bookingId = currentState.bookingId
        if (bookingId <= 0L || currentState.isUpdatingStatus) return

        viewModelScope.launch {
            refreshLocationInternal()
            if (!_uiState.value.isWithinGeofence) {
                _uiState.update { it.copy(errorMessage = GEOFENCE_WARNING_DROPOFF) }
                return@launch
            }

            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
            val result = completeTripUseCase(bookingId, currentState.paymentMethod)
            when (result) {
                is CompleteRideBookingResult.Success -> {
                    driverTripSessionManager.clearActiveTrip()
                    _uiState.update {
                        it.copy(
                            status = BookingStatus.COMPLETED,
                            isUpdatingStatus = false,
                            showSettlementDialog = true,
                            price = if (result.booking.price > 0.0) result.booking.price else it.price
                        )
                    }
                }
                is CompleteRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingStatus = false,
                            errorMessage = result.message ?: "Failed to complete trip"
                        )
                    }
                }
            }
        }
    }

    fun showCancelConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showCancelConfirmDialog = show) }
    }

    fun onConfirmCancelTrip() {
        val bookingId = _uiState.value.bookingId
        if (bookingId <= 0L || _uiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, showCancelConfirmDialog = false) }
            val result = rejectRideBookingUseCase(bookingId)
            when (result) {
                is RejectRideBookingResult.Success -> {
                    driverTripSessionManager.clearActiveTrip()
                    _uiState.update {
                        it.copy(
                            status = BookingStatus.CANCELLED,
                            isUpdatingStatus = false,
                            isTripCancelled = true
                        )
                    }
                }
                is RejectRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingStatus = false,
                            errorMessage = result.message ?: "Failed to cancel trip"
                        )
                    }
                }
            }
        }
    }

    private suspend fun updateBookingSuccess(booking: Booking, fallbackStatus: BookingStatus) {
        val newStatus = booking.status
        val updatedBooking = booking.copy(status = newStatus)
        driverTripSessionManager.saveActiveTrip(updatedBooking)
        _uiState.update {
            it.copy(
                status = newStatus,
                isUpdatingStatus = false,
                price = if (booking.price > 0.0) booking.price else it.price
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissSettlementDialog() {
        _uiState.update { it.copy(showSettlementDialog = false) }
    }

    override fun onCleared() {
        super.onCleared()
        locationPollingJob?.cancel()
    }
}
