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
    private val rideBookingStatusCoordinator: RideBookingStatusCoordinator
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
        rideBookingStatusCoordinator.restoreSessionIfAvailable(
            scope = viewModelScope,
            updateState = { _uiState.update(it) },
            getState = { _uiState.value }
        )
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
        recalculatePricingIfNeeded()
    }

    fun setDropoffLocation(location: LocationPoint) {
        _uiState.update { it.copy(dropoffLocation = location, errorMessage = null) }
        recalculatePricingIfNeeded()
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
            recalculatePricingIfNeeded()
        }
    }

    private fun recalculatePricingIfNeeded() {
        val pickup = _uiState.value.pickupLocation
        val dropoff = _uiState.value.dropoffLocation

        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(pricingEstimate = null, pricingError = null) }
            return
        }

        if (pickup.latitude == dropoff.latitude && pickup.longitude == dropoff.longitude) {
            _uiState.update {
                it.copy(
                    pricingEstimate = null,
                    pricingError = "Điểm đón và điểm đến không thể trùng nhau"
                )
            }
            return
        }

        priceCalculationJob?.cancel()
        priceCalculationJob = viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingPrice = true, pricingError = null) }
            val result = calculateRidePricingUseCase(
                startLatitude = pickup.latitude,
                startLongitude = pickup.longitude,
                endLatitude = dropoff.latitude,
                endLongitude = dropoff.longitude
            )
            when (result) {
                is CalculateRidePricingResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isCalculatingPrice = false,
                            pricingEstimate = result.estimate,
                            pricingError = null
                        )
                    }
                }
                is CalculateRidePricingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCalculatingPrice = false,
                            pricingEstimate = null,
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

        if (_uiState.value.isCreatingBooking || _uiState.value.isSearchingDriver) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBooking = true, errorMessage = null) }
            val result = createRideBookingUseCase(
                startLatitude = pickup.latitude,
                startLongitude = pickup.longitude,
                endLatitude = dropoff.latitude,
                endLongitude = dropoff.longitude
            )
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
                    rideBookingStatusCoordinator.startObserving(
                        scope = viewModelScope,
                        bookingId = booking.bookingId,
                        updateState = { _uiState.update(it) },
                        getState = { _uiState.value }
                    )
                    if (booking.status == BookingStatus.PENDING) {
                        startSearchCountdown()
                    }
                }
                is CreateRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingBooking = false,
                            errorMessage = result.message ?: "Không thể tạo yêu cầu đặt xe"
                        )
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
            // Timeout reached (60 seconds elapsed without matching)
            _uiState.update {
                it.copy(
                    isSearchingDriver = false,
                    searchCountdownSeconds = 0,
                    infoMessage = "Hết thời gian tìm kiếm tài xế. Vui lòng thử lại sau ít phút."
                )
            }
        }
    }

    fun cancelBooking() {
        val booking = _uiState.value.currentBooking
        if (booking == null) {
            // If just searching UI without active backend bookingId, close directly
            countdownJob?.cancel()
            rideBookingStatusCoordinator.stopObserving()
            _uiState.update { it.copy(isSearchingDriver = false) }
            return
        }

        if (_uiState.value.isCancellingBooking) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancellingBooking = true, errorMessage = null) }
            val result = cancelRideBookingUseCase(booking.bookingId)
            when (result) {
                is CancelRideBookingResult.Success -> {
                    countdownJob?.cancel()
                    rideBookingStatusCoordinator.stopObserving()
                    rideBookingStatusCoordinator.clearSession()
                    _uiState.update {
                        it.copy(
                            isCancellingBooking = false,
                            isSearchingDriver = false,
                            currentBooking = null,
                            assignedDriver = null,
                            infoMessage = "Đã hủy chuyến xe thành công."
                        )
                    }
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
        _uiState.update {
            it.copy(
                currentBooking = null,
                assignedDriver = null,
                tripStatusMessage = null
            )
        }
        viewModelScope.launch {
            rideBookingStatusCoordinator.clearSession()
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
