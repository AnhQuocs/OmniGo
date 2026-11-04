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
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.usecase.GetVouchersUseCase
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val detectCurrentLocationUseCase: DetectCurrentLocationUseCase,
    private val userLocationManager: UserLocationManager,
    private val sessionManager: SessionManager,
    private val homeAddressCoordinator: HomeAddressCoordinator,
    private val homeFilterCoordinator: HomeFilterCoordinator,
    private val rideTrackingSessionManager: RideTrackingSessionManager,
    private val cancelRideBookingUseCase: CancelRideBookingUseCase,
    private val getVouchersUseCase: GetVouchersUseCase,
    private val getWalletUseCase: com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverWalletUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observeUserLocation()
        observeSavedAddresses()
        fetchPopularPlaces()
        fetchHomeVouchers()
        loadProvinces()
        observeActiveRideSession()
        restoreActiveRideSession()
        loadWalletBalance()
    }

    fun onResume() {
        restoreActiveRideSession()
        loadWalletBalance()
    }

    private fun loadWalletBalance() {
        _uiState.update { it.copy(isWalletLoading = true) }
        viewModelScope.launch {
            getWalletUseCase?.invoke()?.onSuccess { wallet ->
                val formatted = "${java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(wallet.balance.toLong())}đ"
                _uiState.update { it.copy(walletBalanceFormatted = formatted, isWalletLoading = false) }
            }?.onFailure {
                _uiState.update { it.copy(isWalletLoading = false) }
            } ?: _uiState.update { it.copy(isWalletLoading = false) }
        }
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
                        val filtered = homeFilterCoordinator.filterAndSort(
                            it.popularPlaces,
                            it.selectedFilter,
                            location.latitude,
                            location.longitude
                        )
                        it.copy(
                            userLocation = location,
                            displayAddress = address,
                            filteredPopularPlaces = filtered.take(MAX_SUGGESTED_PLACES),
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
        fetchHomeVouchers()
        if (_uiState.value.userLocation == null) {
            detectLocation()
        }
        restoreActiveRideSession()
        loadWalletBalance()
    }

    private fun fetchPopularPlaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPopularPlacesLoading = true) }
            when (val result = getRestaurantsUseCase(search = null)) {
                is GetRestaurantsResult.Success -> {
                    val allPlaces = result.restaurants
                    val filtered = homeFilterCoordinator.filterAndSort(
                        allPlaces,
                        _uiState.value.selectedFilter,
                        _uiState.value.userLocation?.latitude,
                        _uiState.value.userLocation?.longitude
                    )
                    _uiState.update {
                        it.copy(
                            isPopularPlacesLoading = false,
                            popularPlaces = allPlaces,
                            filteredPopularPlaces = filtered.take(MAX_SUGGESTED_PLACES)
                        )
                    }
                }
                is GetRestaurantsResult.Error -> {
                    _uiState.update { it.copy(isPopularPlacesLoading = false) }
                }
            }
        }
    }

    fun selectFilter(filter: HomeFilter) {
        val filtered = homeFilterCoordinator.filterAndSort(
            _uiState.value.popularPlaces,
            filter,
            _uiState.value.userLocation?.latitude,
            _uiState.value.userLocation?.longitude
        )
        _uiState.update {
            it.copy(selectedFilter = filter, filteredPopularPlaces = filtered.take(MAX_SUGGESTED_PLACES))
        }
    }

    fun toggleFavoriteRestaurant(restaurantId: Long) {
        _uiState.update { state ->
            val updated = if (state.favoriteRestaurantIds.contains(restaurantId)) {
                state.favoriteRestaurantIds - restaurantId
            } else {
                state.favoriteRestaurantIds + restaurantId
            }
            state.copy(favoriteRestaurantIds = updated)
        }
    }

    fun openNotifications() = _uiState.update { it.copy(showNotificationsSheet = true) }
    fun dismissNotifications() = _uiState.update { it.copy(showNotificationsSheet = false) }
    fun markNotificationsAsRead() = _uiState.update { it.copy(hasUnreadNotifications = false, showNotificationsSheet = false) }

    fun openQrDialog() = _uiState.update { it.copy(showQrDialog = true) }
    fun dismissQrDialog() = _uiState.update { it.copy(showQrDialog = false) }

    fun fetchHomeVouchers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isVouchersLoading = true) }
            getVouchersUseCase().onSuccess { list ->
                _uiState.update { it.copy(vouchers = list, isVouchersLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isVouchersLoading = false) }
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

    companion object {
        private const val MAX_SUGGESTED_PLACES = 3
    }
}
