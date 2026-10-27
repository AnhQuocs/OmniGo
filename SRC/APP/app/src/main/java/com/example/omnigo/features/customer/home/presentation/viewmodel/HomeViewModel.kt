package com.example.omnigo.features.customer.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantsUseCase
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
import com.example.omnigo.features.customer.home.presentation.ui.components.AddressPickerStep
import com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isPopularPlacesLoading: Boolean = false,
    val popularPlaces: List<Restaurant> = emptyList(),
    val userLocation: UserLocation? = null,
    val displayAddress: String? = null,
    val isLocationLoading: Boolean = false,
    val showLocationPromptDialog: Boolean = false,
    val showAddressPickerSheet: Boolean = false,
    val addressPickerStep: AddressPickerStep = AddressPickerStep.PROVINCE,
    val provinces: List<Province> = emptyList(),
    val districts: List<District> = emptyList(),
    val wards: List<Ward> = emptyList(),
    val selectedProvince: Province? = null,
    val selectedDistrict: District? = null,
    val selectedWard: Ward? = null,
    val streetDetail: String = "",
    val searchQuery: String = "",
    val savedAddresses: List<SavedAddress> = emptyList(),
    val editingAddressId: String? = null,
    val activeRideState: ActiveRideState? = null,
    val showActiveTripSheet: Boolean = false,
    val isCancellingActiveTrip: Boolean = false,
    val showCancelTripConfirmDialog: Boolean = false,
    val cancelTripErrorMessage: String? = null
)

sealed interface HomeUiEvent {
    data object OpenLocationSettings : HomeUiEvent
    data object OpenAppSettings : HomeUiEvent
    data object RequestLocationPermission : HomeUiEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val detectCurrentLocationUseCase: DetectCurrentLocationUseCase,
    private val userLocationManager: UserLocationManager,
    private val sessionManager: SessionManager,
    private val homeAddressCoordinator: HomeAddressCoordinator,
    private val rideTrackingSessionManager: RideTrackingSessionManager,
    private val cancelRideBookingUseCase: CancelRideBookingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observeUserLocation()
        observeSavedAddresses()
        fetchPopularPlaces()
        loadProvinces()
        observeActiveRideSession()
        restoreActiveRideSession()
    }

    fun onResume() {
        restoreActiveRideSession()
    }

    private fun restoreActiveRideSession() {
        viewModelScope.launch {
            rideTrackingSessionManager.restoreSessionIfAvailable()
        }
    }

    private fun observeActiveRideSession() {
        viewModelScope.launch {
            rideTrackingSessionManager.activeRideState.collect { activeRide ->
                _uiState.update {
                    it.copy(
                        activeRideState = activeRide,
                        showActiveTripSheet = if (activeRide == null || activeRide.isFinished) {
                            false
                        } else {
                            it.showActiveTripSheet
                        }
                    )
                }
            }
        }
    }

    private fun observeSavedAddresses() {
        viewModelScope.launch {
            homeAddressCoordinator.getSavedAddresses().collect { list ->
                _uiState.update { it.copy(savedAddresses = list) }
            }
        }
    }

    private fun observeUserLocation() {
        viewModelScope.launch {
            userLocationManager.currentLocation.collect { location ->
                if (location != null) {
                    val address = if (location.addressName.isNotBlank()) {
                        location.addressName
                    } else {
                        location.fullAddress
                    }
                    _uiState.update {
                        it.copy(
                            userLocation = location,
                            displayAddress = address,
                            isLocationLoading = false
                        )
                    }
                } else if (_uiState.value.userLocation == null) {
                    detectLocation()
                }
            }
        }
    }

    fun refresh() {
        fetchPopularPlaces()
        if (_uiState.value.userLocation == null) {
            detectLocation()
        }
        restoreActiveRideSession()
    }

    private fun fetchPopularPlaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPopularPlacesLoading = true) }
            when (val result = getRestaurantsUseCase(search = null)) {
                is GetRestaurantsResult.Success -> {
                    val topPlaces = result.restaurants.take(3)
                    _uiState.update {
                        it.copy(isPopularPlacesLoading = false, popularPlaces = topPlaces)
                    }
                }
                is GetRestaurantsResult.Error -> {
                    _uiState.update { it.copy(isPopularPlacesLoading = false) }
                }
            }
        }
    }

    fun detectLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocationLoading = true) }
            when (val result = detectCurrentLocationUseCase()) {
                is DetectLocationResult.Success -> {
                    userLocationManager.updateLocation(result.location)
                    sessionManager.saveUserLocation(result.location)
                    val address = result.location.addressName.ifBlank { result.location.fullAddress }
                    _uiState.update {
                        it.copy(
                            isLocationLoading = false,
                            userLocation = result.location,
                            displayAddress = address,
                            showLocationPromptDialog = false
                        )
                    }
                }
                is DetectLocationResult.GpsDisabled,
                is DetectLocationResult.PermissionRequired,
                is DetectLocationResult.LocationUnavailable -> {
                    _uiState.update { it.copy(isLocationLoading = false) }
                }
            }
        }
    }

    fun onLocationHeaderClick() {
        _uiState.update { it.copy(showLocationPromptDialog = true) }
    }

    fun onEnableGpsClicked(hasPermission: Boolean, isGpsOn: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(showLocationPromptDialog = false) }
            if (!hasPermission) {
                _uiEvent.send(HomeUiEvent.RequestLocationPermission)
            } else if (!isGpsOn) {
                _uiEvent.send(HomeUiEvent.OpenLocationSettings)
            } else {
                detectLocation()
            }
        }
    }

    fun onOpenManualPickerClicked() {
        _uiState.update {
            it.copy(
                showLocationPromptDialog = false,
                showAddressPickerSheet = true,
                addressPickerStep = AddressPickerStep.PROVINCE,
                searchQuery = ""
            )
        }
        loadProvinces()
    }

    fun dismissLocationPrompt() {
        _uiState.update { it.copy(showLocationPromptDialog = false) }
    }

    fun dismissAddressPicker() {
        _uiState.update { it.copy(showAddressPickerSheet = false) }
    }

    fun onSearchQueryChange(query: String) {
        homeAddressCoordinator.onSearchQueryChange(viewModelScope, query, _uiState.value) { _uiState.update(it) }
    }

    private fun loadProvinces() {
        homeAddressCoordinator.loadProvinces(viewModelScope) { _uiState.update(it) }
    }

    fun onSelectProvince(province: Province) {
        homeAddressCoordinator.onSelectProvince(viewModelScope, province) { _uiState.update(it) }
    }

    fun onSelectDistrict(district: District) {
        val provinceId = _uiState.value.selectedProvince?.id ?: return
        homeAddressCoordinator.onSelectDistrict(viewModelScope, provinceId, district) { _uiState.update(it) }
    }

    fun onSelectWard(ward: Ward) {
        _uiState.update {
            it.copy(
                selectedWard = ward,
                addressPickerStep = AddressPickerStep.STREET_DETAIL,
                searchQuery = ""
            )
        }
    }

    fun onStreetDetailChange(text: String) {
        _uiState.update { it.copy(streetDetail = text) }
    }

    fun onStepChange(step: AddressPickerStep) {
        _uiState.update { it.copy(addressPickerStep = step, searchQuery = "") }
    }

    fun onSelectSavedAddress(savedAddress: SavedAddress) {
        homeAddressCoordinator.onSelectSavedAddress(viewModelScope, savedAddress) { _uiState.update(it) }
    }

    fun onEditSavedAddress(savedAddress: SavedAddress) {
        homeAddressCoordinator.onEditSavedAddress(viewModelScope, savedAddress) { _uiState.update(it) }
    }

    fun onDeleteSavedAddress(savedAddress: SavedAddress) {
        homeAddressCoordinator.onDeleteSavedAddress(viewModelScope, savedAddress)
    }

    fun onConfirmCustomAddress() {
        homeAddressCoordinator.onConfirmCustomAddress(viewModelScope, _uiState.value) { _uiState.update(it) }
    }

    fun onActiveTripBannerClick() {
        if (_uiState.value.activeRideState != null) {
            _uiState.update { it.copy(showActiveTripSheet = true) }
        }
    }

    fun dismissActiveTripSheet() {
        _uiState.update { it.copy(showActiveTripSheet = false) }
    }

    fun promptCancelActiveTrip() {
        _uiState.update { it.copy(showCancelTripConfirmDialog = true, cancelTripErrorMessage = null) }
    }

    fun dismissCancelTripDialog() {
        _uiState.update { it.copy(showCancelTripConfirmDialog = false, cancelTripErrorMessage = null) }
    }

    fun confirmCancelActiveTrip() {
        val activeBookingId = _uiState.value.activeRideState?.bookingId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancellingActiveTrip = true, cancelTripErrorMessage = null) }
            when (val result = cancelRideBookingUseCase(activeBookingId)) {
                is CancelRideBookingResult.Success -> {
                    rideTrackingSessionManager.clearSession()
                    _uiState.update {
                        it.copy(
                            isCancellingActiveTrip = false,
                            showCancelTripConfirmDialog = false,
                            showActiveTripSheet = false
                        )
                    }
                }
                is CancelRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCancellingActiveTrip = false,
                            cancelTripErrorMessage = result.message ?: "Không thể hủy chuyến xe lúc này"
                        )
                    }
                }
            }
        }
    }
}
