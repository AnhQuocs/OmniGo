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
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
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

import com.example.omnigo.core.location.tracker.OsrmRoutingService
import kotlinx.coroutines.async
import com.example.omnigo.R
import com.example.omnigo.utils.UiText

@HiltViewModel
class RideBookingViewModel @Inject constructor(
    private val calculateRidePricingUseCase: CalculateRidePricingUseCase,
    private val createRideBookingUseCase: CreateRideBookingUseCase,
    private val cancelRideBookingUseCase: CancelRideBookingUseCase,
    private val detectCurrentLocationUseCase: DetectCurrentLocationUseCase,
    private val savedAddressRepository: SavedAddressRepository,
    private val rideBookingStatusCoordinator: RideBookingStatusCoordinator,
    private val rideTrackingSessionManager: RideTrackingSessionManager,
    private val getNearbyDriversUseCase: GetNearbyDriversUseCase,
    private val ridePlaceSearchCoordinator: RidePlaceSearchCoordinator,
    private val osrmRoutingService: OsrmRoutingService
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
                    if (active.status == BookingStatus.ACCEPTED || active.status == BookingStatus.COMPLETED) {
                        cancelSearchCountdown()
                    }
                    _uiState.update { curr ->
                        val updatedBooking = curr.currentBooking?.copy(
                            status = active.status,
                            driverId = active.driverProfile?.driverId ?: curr.currentBooking?.driverId,
                            price = if (active.price > 0.0) active.price else curr.currentBooking?.price ?: 0.0
                        ) ?: if (active.bookingId > 0L) Booking(bookingId = active.bookingId, status = active.status, price = active.price, driverId = active.driverProfile?.driverId) else null
                        val isDone = active.status == BookingStatus.COMPLETED
                        curr.copy(
                            currentBooking = updatedBooking,
                            completedBooking = if (isDone) (updatedBooking ?: curr.completedBooking) else curr.completedBooking,
                            showTripCompletedReceipt = if (isDone) true else curr.showTripCompletedReceipt,
                            assignedDriver = active.driverProfile ?: curr.assignedDriver,
                            driverLiveLocation = active.driverLiveLocation ?: curr.driverLiveLocation,
                            liveDistanceKm = active.liveDistanceKm ?: curr.liveDistanceKm,
                            liveEtaMinutes = active.liveEtaMinutes ?: curr.liveEtaMinutes,
                            isSearchingDriver = active.isSearchingDriver,
                            tripStatusMessage = active.statusMessage ?: curr.tripStatusMessage,
                            activeTripRouteCoordinates = if (active.routeCoordinates.isNotEmpty()) active.routeCoordinates else curr.activeTripRouteCoordinates
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
                    _uiState.update { it.copy(pickupLocation = locationPoint, isLocatingCurrentPosition = false) }
                    loadNearbyDrivers(locationPoint.latitude, locationPoint.longitude)
                    recalculatePricingIfNeeded()
                }
                else -> _uiState.update { it.copy(isLocatingCurrentPosition = false) }
            }
        }
    }

    fun setPickupLocation(location: LocationPoint) {
        _uiState.update { it.copy(pickupLocation = location, isPickupPinConfirmed = true, errorMessage = null) }
        loadNearbyDrivers(location.latitude, location.longitude); recalculatePricingIfNeeded()
    }

    fun setDropoffLocation(location: LocationPoint) {
        _uiState.update { it.copy(dropoffLocation = location, errorMessage = null) }
        recalculatePricingIfNeeded()
    }

    fun selectVehicleType(type: VehicleType) {
        if (_uiState.value.selectedVehicleType == type) return
        val estimate = _uiState.value.vehicleEstimates[type]
        _uiState.update {
            it.copy(
                selectedVehicleType = type, pricingEstimate = estimate,
                pricingError = if (estimate == null && !it.isCalculatingPrice) UiText.StringResource(R.string.ride_pricing_unavailable) else null
            )
        }
        updateRouteForSelectedVehicle()
    }

    private fun updateRouteForSelectedVehicle() {
        val pickup = _uiState.value.pickupLocation ?: return
        val dropoff = _uiState.value.dropoffLocation ?: return
        val selectedType = _uiState.value.selectedVehicleType
        viewModelScope.launch {
            val coords = osrmRoutingService.getRouteCoordinates(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, selectedType.name)
            if (coords.isNotEmpty()) _uiState.update { it.copy(routeCoordinates = coords) }
        }
    }

    fun openPlaceSearch(target: LocationTarget) = ridePlaceSearchCoordinator.openPlaceSearch(target) { _uiState.update(it) }
    fun closePlaceSearch() = ridePlaceSearchCoordinator.closePlaceSearch { _uiState.update(it) }
    fun updateSearchQuery(query: String) = ridePlaceSearchCoordinator.updateSearchQuery(viewModelScope, query) { _uiState.update(it) }

    fun selectSearchResult(point: LocationPoint) {
        val target = _uiState.value.searchTarget
        ridePlaceSearchCoordinator.closePlaceSearch { _uiState.update(it) }
        if (target == LocationTarget.DROPOFF) {
            _uiState.update { it.copy(dropoffLocation = point, pendingDropoffPinLocation = null) }
            if (!_uiState.value.isPickupPinConfirmed && _uiState.value.pickupLocation != null) {
                startMapPinPicking(LocationTarget.PICKUP, _uiState.value.pickupLocation)
            } else { recalculatePricingIfNeeded() }
        } else {
            startMapPinPicking(LocationTarget.PICKUP, point)
        }
    }

    fun selectCurrentLocation(currentLocationName: String) {
        val target = _uiState.value.searchTarget
        ridePlaceSearchCoordinator.closePlaceSearch { _uiState.update(it) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLocatingCurrentPosition = true) }
            when (val result = detectCurrentLocationUseCase()) {
                is DetectLocationResult.Success -> {
                    val point = LocationPoint(currentLocationName, result.location.fullAddress.ifBlank { result.location.addressName }, result.location.latitude, result.location.longitude, isCurrentLocation = true)
                    _uiState.update { it.copy(isLocatingCurrentPosition = false) }
                    if (target == LocationTarget.PICKUP) {
                        _uiState.update { it.copy(pickupLocation = point, isPickupPinConfirmed = false) }
                        startMapPinPicking(LocationTarget.PICKUP, point)
                    } else {
                        _uiState.update { it.copy(dropoffLocation = point, pendingDropoffPinLocation = null) }
                        if (!_uiState.value.isPickupPinConfirmed && _uiState.value.pickupLocation != null) startMapPinPicking(LocationTarget.PICKUP, _uiState.value.pickupLocation) else recalculatePricingIfNeeded()
                    }
                }
                else -> _uiState.update { it.copy(isLocatingCurrentPosition = false, errorMessage = UiText.StringResource(R.string.ride_gps_required_msg)) }
            }
        }
    }

    fun startMapPinPicking(target: LocationTarget, initialPoint: LocationPoint? = null) {
        closePlaceSearch()
        _uiState.update { it.copy(isMapPinPicking = true, pinPickingTarget = target, pinPickedLocation = initialPoint) }
    }

    fun confirmMapPinLocation(point: LocationPoint) {
        if (_uiState.value.pinPickingTarget == LocationTarget.PICKUP) {
            loadNearbyDrivers(point.latitude, point.longitude)
            val currentDropoff = _uiState.value.dropoffLocation
            val info = if (currentDropoff == null) UiText.StringResource(R.string.ride_pickup_confirmed_hint) else null
            _uiState.update { it.copy(isMapPinPicking = false, pinPickingTarget = null, pinPickedLocation = null, pickupLocation = point, isPickupPinConfirmed = true, pendingDropoffPinLocation = null, infoMessage = info) }
            if (currentDropoff == null) openPlaceSearch(LocationTarget.DROPOFF) else recalculatePricingIfNeeded()
        } else {
            _uiState.update { it.copy(isMapPinPicking = false, pinPickingTarget = null, pinPickedLocation = null, dropoffLocation = point, pendingDropoffPinLocation = null) }
            recalculatePricingIfNeeded()
        }
    }

    fun cancelMapPinPicking() = _uiState.update {
        it.copy(isMapPinPicking = false, pinPickingTarget = null, pinPickedLocation = null, pendingDropoffPinLocation = null)
    }

    fun loadNearbyDrivers(lat: Double, lng: Double) = viewModelScope.launch {
        val drivers = getNearbyDriversUseCase(lat, lng)
        drivers.forEach { rideTrackingSessionManager.registerDriverLocation(it.driverId, it.latitude, it.longitude) }
        _uiState.update { it.copy(nearbyDrivers = drivers) }
    }

    fun swapLocations() {
        val currentPickup = _uiState.value.pickupLocation
        val currentDropoff = _uiState.value.dropoffLocation
        if (currentPickup != null || currentDropoff != null) {
            _uiState.update {
                it.copy(pickupLocation = currentDropoff, dropoffLocation = currentPickup, isPickupPinConfirmed = true, errorMessage = null)
            }
            if (currentDropoff != null) loadNearbyDrivers(currentDropoff.latitude, currentDropoff.longitude)
            recalculatePricingIfNeeded()
        }
    }

    private fun recalculatePricingIfNeeded() {
        val pickup = _uiState.value.pickupLocation
        val dropoff = _uiState.value.dropoffLocation
        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(pricingEstimate = null, vehicleEstimates = emptyMap(), pricingError = null, routeCoordinates = emptyList()) }
            return
        }
        if (pickup.latitude == dropoff.latitude && pickup.longitude == dropoff.longitude) {
            _uiState.update { it.copy(pricingEstimate = null, vehicleEstimates = emptyMap(), pricingError = UiText.StringResource(R.string.ride_error_same_locations), routeCoordinates = emptyList()) }
            return
        }

        val selectedType = _uiState.value.selectedVehicleType
        priceCalculationJob?.cancel()
        priceCalculationJob = viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingPrice = true, pricingError = null) }
            val bikeDeferred = async { calculateRidePricingUseCase(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, vehicleType = "BIKE") }
            val car4Deferred = async { calculateRidePricingUseCase(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, vehicleType = "CAR_4_SEAT") }
            val car7Deferred = async { calculateRidePricingUseCase(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, vehicleType = "CAR_7_SEAT") }
            val routeDeferred = async { osrmRoutingService.getRouteCoordinates(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, selectedType.name) }

            val bikeRes = bikeDeferred.await()
            val car4Res = car4Deferred.await()
            val car7Res = car7Deferred.await()
            val routeCoords = routeDeferred.await()

            val estimates = buildMap {
                if (bikeRes is CalculateRidePricingResult.Success) put(VehicleType.BIKE, bikeRes.estimate)
                if (car4Res is CalculateRidePricingResult.Success) put(VehicleType.CAR_4_SEAT, car4Res.estimate)
                if (car7Res is CalculateRidePricingResult.Success) put(VehicleType.CAR_7_SEAT, car7Res.estimate)
            }
            val selectedResult = when (selectedType) {
                VehicleType.BIKE -> bikeRes
                VehicleType.CAR_4_SEAT -> car4Res
                VehicleType.CAR_7_SEAT -> car7Res
                else -> null
            }
            val errorUiText: UiText? = when {
                estimates.isEmpty() -> (bikeRes as? CalculateRidePricingResult.Error)?.message?.let { UiText.DynamicString(it) }
                    ?: (car4Res as? CalculateRidePricingResult.Error)?.message?.let { UiText.DynamicString(it) }
                    ?: (car7Res as? CalculateRidePricingResult.Error)?.message?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.ride_pricing_failed)
                selectedResult is CalculateRidePricingResult.Error -> selectedResult.message?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.ride_pricing_failed)
                else -> null
            }

            _uiState.update {
                it.copy(isCalculatingPrice = false, pricingEstimate = estimates[selectedType], vehicleEstimates = estimates, pricingError = errorUiText, routeCoordinates = routeCoords)
            }
        }
    }

    fun createBooking() {
        val pickup = _uiState.value.pickupLocation
        val dropoff = _uiState.value.dropoffLocation
        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.ride_error_incomplete_locations)) }
            return
        }
        if (_uiState.value.isCreatingBooking || _uiState.value.isSearchingDriver) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBooking = true, errorMessage = null) }
            val selectedType = _uiState.value.selectedVehicleType
            val result = createRideBookingUseCase(
                startLatitude = pickup.latitude, startLongitude = pickup.longitude, endLatitude = dropoff.latitude, endLongitude = dropoff.longitude,
                vehicleType = selectedType.name, pickupAddress = pickup.name.ifBlank { pickup.address }, dropoffAddress = dropoff.name.ifBlank { dropoff.address }
            )
            when (result) {
                is CreateRideBookingResult.Success -> {
                    val booking = result.booking
                    if (booking.status == BookingStatus.CANCELLED) {
                        _uiState.update { it.copy(isCreatingBooking = false, isSearchingDriver = false, currentBooking = null, errorMessage = UiText.StringResource(R.string.ride_error_no_driver_found)) }
                    } else {
                        _uiState.update { it.copy(isCreatingBooking = false, currentBooking = booking, isSearchingDriver = booking.status == BookingStatus.PENDING, searchCountdownSeconds = 60) }
                        _eventFlow.emit(RideBookingEvent.BookingCreated(booking.bookingId))
                        rideTrackingSessionManager.startSession(booking, pickup.name.ifBlank { pickup.address }, dropoff.name.ifBlank { dropoff.address })
                        rideBookingStatusCoordinator.startObserving(viewModelScope, booking.bookingId, { _uiState.update(it) }, { _uiState.value }, { cancelSearchCountdown() })
                        if (booking.status == BookingStatus.PENDING) startSearchCountdown()
                    }
                }
                is CreateRideBookingResult.Error -> {
                    val errorUi = result.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.ride_error_create_failed)
                    _uiState.update { it.copy(isCreatingBooking = false, errorMessage = errorUi) }
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

    fun cancelSearchCountdown() { countdownJob?.cancel(); _uiState.update { it.copy(showSearchTimeoutDialog = false) } }

    fun retrySearchDriver() {
        _uiState.update { it.copy(showSearchTimeoutDialog = false, isSearchingDriver = true, searchCountdownSeconds = 60) }
        startSearchCountdown()
    }

    fun dismissSearchTimeoutDialog() { cancelSearchCountdown(); cancelBooking() }

    fun cancelBooking() {
        val booking = _uiState.value.currentBooking ?: run { resetBookingState(); return }
        if (_uiState.value.isCancellingBooking) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancellingBooking = true, errorMessage = null) }
            when (val result = cancelRideBookingUseCase(booking.bookingId)) {
                is CancelRideBookingResult.Success -> {
                    resetBookingState(UiText.StringResource(R.string.ride_booking_cancelled_success))
                    _eventFlow.emit(RideBookingEvent.BookingCancelled)
                }
                is CancelRideBookingResult.Error -> {
                    val errorUi = result.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.ride_error_cancel_failed)
                    _uiState.update { it.copy(isCancellingBooking = false, errorMessage = errorUi) }
                }
            }
        }
    }

    fun dismissRadar() { cancelSearchCountdown(); _uiState.update { it.copy(isSearchingDriver = false) } }
    fun dismissCompletedTrip() {
        resetBookingState()
        _uiState.update { it.copy(dropoffLocation = null, pricingEstimate = null, vehicleEstimates = emptyMap(), pricingError = null, routeCoordinates = emptyList()) }
        _uiState.value.pickupLocation?.let { loadNearbyDrivers(it.latitude, it.longitude) }
    }

    private fun resetBookingState(infoMsg: UiText? = null) {
        cancelSearchCountdown()
        rideBookingStatusCoordinator.stopObserving()
        viewModelScope.launch { rideBookingStatusCoordinator.clearSession() }
        rideTrackingSessionManager.clearSession()
        _uiState.update {
            it.copy(isCancellingBooking = false, isSearchingDriver = false, currentBooking = null, assignedDriver = null,
                tripStatusMessage = null, driverLiveLocation = null, liveDistanceKm = null, liveEtaMinutes = null,
                showTripCompletedReceipt = false, completedBooking = null, activeTripRouteCoordinates = emptyList(), infoMessage = infoMsg ?: it.infoMessage)
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, infoMessage = null) }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel(); priceCalculationJob?.cancel()
        rideBookingStatusCoordinator.stopObserving(); ridePlaceSearchCoordinator.cancel()
    }
}
