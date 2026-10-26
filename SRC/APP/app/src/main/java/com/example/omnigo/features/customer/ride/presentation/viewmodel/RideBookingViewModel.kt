package com.example.omnigo.features.customer.ride.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.usecase.CalculateRidePricingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.CreateRideBookingUseCase
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.usecase.GetNearbyDriversUseCase
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RideBookingEvent {
    data class ShowToast(val message: String) : RideBookingEvent
    data class BookingCreated(val bookingId: Long) : RideBookingEvent
    data object BookingCancelled : RideBookingEvent
}

@HiltViewModel
class RideBookingViewModel @Inject constructor(
    private val calculateRidePricingUseCase: CalculateRidePricingUseCase,
    private val createRideBookingUseCase: CreateRideBookingUseCase,
    private val cancelRideBookingUseCase: CancelRideBookingUseCase,
    private val detectCurrentLocationUseCase: DetectCurrentLocationUseCase,
    private val savedAddressRepository: SavedAddressRepository,
    private val rideBookingStatusCoordinator: RideBookingStatusCoordinator,
    private val rideTrackingSessionManager: RideTrackingSessionManager,
    private val getNearbyDriversUseCase: GetNearbyDriversUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideBookingUiState())
    val uiState: StateFlow<RideBookingUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RideBookingEvent>()
    val eventFlow: SharedFlow<RideBookingEvent> = _eventFlow.asSharedFlow()

    private var countdownJob: Job? = null
    private var priceCalculationJob: Job? = null

    init {
        loadSavedAddresses()
        autoDetectPickupLocation()
        observeActiveRideSession()
        rideBookingStatusCoordinator.restoreSessionIfAvailable(
            scope = viewModelScope,
            updateState = { _uiState.update(it) },
            getState = { _uiState.value }
        )
    }

    private fun observeActiveRideSession() {
        viewModelScope.launch {
            rideTrackingSessionManager.activeRideState.collect { active ->
                if (active != null) {
                    _uiState.update { curr ->
                        curr.copy(
                            currentBooking = curr.currentBooking?.copy(
                                status = active.status,
                                driverId = active.driverProfile?.driverId ?: curr.currentBooking?.driverId,
                                price = if (active.price > 0.0) active.price else curr.currentBooking?.price ?: 0.0
                            ),
                            assignedDriver = active.driverProfile ?: curr.assignedDriver,
                            driverLiveLocation = active.driverLiveLocation ?: curr.driverLiveLocation,
                            liveDistanceKm = active.liveDistanceKm ?: curr.liveDistanceKm,
                            liveEtaMinutes = active.liveEtaMinutes ?: curr.liveEtaMinutes,
                            isSearchingDriver = active.isSearchingDriver,
                            tripStatusMessage = active.statusMessage ?: curr.tripStatusMessage
                        )
                    }
                }
            }
        }
    }

    private fun loadSavedAddresses() {
        viewModelScope.launch {
            savedAddressRepository.getSavedAddresses().collect { addresses ->
                _uiState.update { it.copy(savedAddresses = addresses) }
            }
        }
    }

    fun autoDetectPickupLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocatingCurrentPosition = true) }
            when (val result = detectCurrentLocationUseCase()) {
                is DetectLocationResult.Success -> {
                    val locationPoint = LocationPoint.fromUserLocation(result.location)
                    _uiState.update {
                        it.copy(
                            pickupLocation = locationPoint,
                            isLocatingCurrentPosition = false
                        )
                    }
                    loadNearbyDrivers(locationPoint.latitude, locationPoint.longitude)
                    recalculatePricingIfNeeded()
                }
                else -> {
                    _uiState.update { it.copy(isLocatingCurrentPosition = false) }
                }
            }
        }
    }

    fun setPickupLocation(location: LocationPoint) {
        _uiState.update { it.copy(pickupLocation = location, errorMessage = null) }
        loadNearbyDrivers(location.latitude, location.longitude)
        recalculatePricingIfNeeded()
    }

    fun setDropoffLocation(location: LocationPoint) {
        _uiState.update { it.copy(dropoffLocation = location, errorMessage = null) }
        recalculatePricingIfNeeded()
    }

    fun selectVehicleType(type: VehicleType) {
        if (_uiState.value.selectedVehicleType == type) return
        _uiState.update {
            it.copy(
                selectedVehicleType = type,
                pricingEstimate = it.vehicleEstimates[type] ?: it.pricingEstimate
            )
        }
        recalculatePricingIfNeeded()
    }

    fun startMapPinPicking(target: LocationTarget) {
        _uiState.update { it.copy(isMapPinPicking = true, pinPickingTarget = target) }
    }

    fun confirmMapPinLocation(point: LocationPoint) {
        val target = _uiState.value.pinPickingTarget
        _uiState.update {
            it.copy(
                isMapPinPicking = false,
                pinPickingTarget = null,
                pickupLocation = if (target == LocationTarget.PICKUP) point else it.pickupLocation,
                dropoffLocation = if (target == LocationTarget.DROPOFF) point else it.dropoffLocation
            )
        }
        if (target == LocationTarget.PICKUP) {
            loadNearbyDrivers(point.latitude, point.longitude)
        }
        recalculatePricingIfNeeded()
    }

    fun cancelMapPinPicking() {
        _uiState.update { it.copy(isMapPinPicking = false, pinPickingTarget = null) }
    }

    fun loadNearbyDrivers(lat: Double, lng: Double) {
        viewModelScope.launch {
            val drivers = getNearbyDriversUseCase(lat, lng)
            _uiState.update { it.copy(nearbyDrivers = drivers) }
        }
    }

    fun swapLocations() {
        val currentPickup = _uiState.value.pickupLocation
        val currentDropoff = _uiState.value.dropoffLocation
        if (currentPickup != null || currentDropoff != null) {
            _uiState.update {
                it.copy(
                    pickupLocation = currentDropoff,
                    dropoffLocation = currentPickup,
                    errorMessage = null
                )
            }
            if (currentDropoff != null) {
                loadNearbyDrivers(currentDropoff.latitude, currentDropoff.longitude)
            }
            recalculatePricingIfNeeded()
        }
    }

    private fun recalculatePricingIfNeeded() {
        val pickup = _uiState.value.pickupLocation
        val dropoff = _uiState.value.dropoffLocation

        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(pricingEstimate = null, vehicleEstimates = emptyMap(), pricingError = null) }
            return
        }

        if (pickup.latitude == dropoff.latitude && pickup.longitude == dropoff.longitude) {
            _uiState.update {
                it.copy(
                    pricingEstimate = null,
                    vehicleEstimates = emptyMap(),
                    pricingError = "Điểm đón và điểm đến không thể trùng nhau"
                )
            }
            return
        }

        val selectedType = _uiState.value.selectedVehicleType
        priceCalculationJob?.cancel()
        priceCalculationJob = viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingPrice = true, pricingError = null) }
            val result = calculateRidePricingUseCase(
                startLatitude = pickup.latitude,
                startLongitude = pickup.longitude,
                endLatitude = dropoff.latitude,
                endLongitude = dropoff.longitude,
                vehicleType = selectedType.name
            )
            when (result) {
                is CalculateRidePricingResult.Success -> {
                    val base = result.estimate
                    val estimates = mapOf(
                        VehicleType.BIKE to base,
                        VehicleType.CAR_4_SEAT to base.copy(totalPrice = base.totalPrice * 1.5),
                        VehicleType.CAR_7_SEAT to base.copy(totalPrice = base.totalPrice * 2.0)
                    )
                    _uiState.update {
                        it.copy(
                            isCalculatingPrice = false,
                            pricingEstimate = estimates[selectedType] ?: base,
                            vehicleEstimates = estimates,
                            pricingError = null
                        )
                    }
                }
                is CalculateRidePricingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCalculatingPrice = false,
                            pricingEstimate = null,
                            vehicleEstimates = emptyMap(),
                            pricingError = result.message ?: "Không thể tính cước phí chuyến đi"
                        )
                    }
                }
            }
        }
    }

    fun createBooking() {
        val pickup = _uiState.value.pickupLocation
        val dropoff = _uiState.value.dropoffLocation

        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn đầy đủ điểm đón và điểm đến") }
            return
        }

        if (_uiState.value.isCreatingBooking || _uiState.value.isSearchingDriver) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBooking = true, errorMessage = null) }
            val result = createRideBookingUseCase(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude)
            when (result) {
                is CreateRideBookingResult.Success -> {
                    val booking = result.booking
                    _uiState.update {
                        it.copy(
                            isCreatingBooking = false,
                            currentBooking = booking,
                            isSearchingDriver = booking.status == BookingStatus.PENDING,
                            searchCountdownSeconds = 60
                        )
                    }
                    _eventFlow.emit(RideBookingEvent.BookingCreated(booking.bookingId))
                    rideTrackingSessionManager.startSession(
                        booking = booking,
                        pickupAddress = pickup.name.ifBlank { pickup.address },
                        dropoffAddress = dropoff.name.ifBlank { dropoff.address }
                    )
                    rideBookingStatusCoordinator.startObserving(
                        scope = viewModelScope,
                        bookingId = booking.bookingId,
                        updateState = { _uiState.update(it) },
                        getState = { _uiState.value }
                    )
                    if (booking.status == BookingStatus.PENDING) startSearchCountdown()
                }
                is CreateRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(isCreatingBooking = false, errorMessage = result.message ?: "Không thể tạo yêu cầu đặt xe")
                    }
                }
            }
        }
    }

    private fun startSearchCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (sec in 60 downTo 1) {
                _uiState.update { it.copy(searchCountdownSeconds = sec) }
                delay(1000L)
            }
            _uiState.update { it.copy(searchCountdownSeconds = 0, showSearchTimeoutDialog = true) }
        }
    }

    fun retrySearchDriver() {
        _uiState.update { it.copy(showSearchTimeoutDialog = false, isSearchingDriver = true, searchCountdownSeconds = 60) }
        startSearchCountdown()
    }

    fun dismissSearchTimeoutDialog() {
        _uiState.update { it.copy(showSearchTimeoutDialog = false) }
        cancelBooking()
    }

    fun cancelBooking() {
        val booking = _uiState.value.currentBooking
        if (booking == null) {
            resetBookingState()
            return
        }

        if (_uiState.value.isCancellingBooking) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancellingBooking = true, errorMessage = null) }
            val result = cancelRideBookingUseCase(booking.bookingId)
            when (result) {
                is CancelRideBookingResult.Success -> {
                    resetBookingState("Đã hủy chuyến xe thành công.")
                    _eventFlow.emit(RideBookingEvent.BookingCancelled)
                }
                is CancelRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCancellingBooking = false,
                            errorMessage = result.message ?: "Không thể hủy chuyến xe lúc này"
                        )
                    }
                }
            }
        }
    }

    fun dismissRadar() {
        countdownJob?.cancel()
        _uiState.update { it.copy(isSearchingDriver = false) }
    }

    fun dismissCompletedTrip() {
        resetBookingState()
    }

    private fun resetBookingState(infoMsg: String? = null) {
        countdownJob?.cancel()
        rideBookingStatusCoordinator.stopObserving()
        viewModelScope.launch { rideBookingStatusCoordinator.clearSession() }
        rideTrackingSessionManager.clearSession()
        _uiState.update {
            it.copy(
                isCancellingBooking = false,
                isSearchingDriver = false,
                currentBooking = null,
                assignedDriver = null,
                tripStatusMessage = null,
                driverLiveLocation = null,
                liveDistanceKm = null,
                liveEtaMinutes = null,
                infoMessage = infoMsg ?: it.infoMessage
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        priceCalculationJob?.cancel()
        rideBookingStatusCoordinator.stopObserving()
    }
}
