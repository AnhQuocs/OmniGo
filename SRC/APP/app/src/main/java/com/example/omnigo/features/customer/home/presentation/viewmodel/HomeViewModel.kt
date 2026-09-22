package com.example.omnigo.features.customer.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantsUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
import com.example.omnigo.features.customer.home.domain.usecase.GeocodeAddressUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetDistrictsByProvinceUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetProvincesUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetWardsByDistrictUseCase
import com.example.omnigo.features.customer.home.presentation.ui.components.AddressPickerStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.usecase.DeleteSavedAddressUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetSavedAddressesUseCase
import com.example.omnigo.features.customer.home.domain.usecase.SaveAddressUseCase

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
    val editingAddressId: String? = null
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
    private val geocodeAddressUseCase: GeocodeAddressUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val getDistrictsByProvinceUseCase: GetDistrictsByProvinceUseCase,
    private val getWardsByDistrictUseCase: GetWardsByDistrictUseCase,
    private val userLocationManager: UserLocationManager,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val saveAddressUseCase: SaveAddressUseCase,
    private val deleteSavedAddressUseCase: DeleteSavedAddressUseCase,
    private val sessionManager: SessionManager
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
    }

    private fun observeSavedAddresses() {
        viewModelScope.launch {
            getSavedAddressesUseCase().collect { list ->
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
        detectLocation()
    }

    private fun fetchPopularPlaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPopularPlacesLoading = true) }
            val result = getRestaurantsUseCase(search = null)
            when (result) {
                is GetRestaurantsResult.Success -> {
                    val topPlaces = result.restaurants.take(3)
                    _uiState.update {
                        it.copy(isPopularPlacesLoading = false, popularPlaces = topPlaces)
                    }
                }
                is GetRestaurantsResult.Error -> {
                    _uiState.update {
                        it.copy(isPopularPlacesLoading = false)
                    }
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
        _uiState.update { it.copy(searchQuery = query) }
        val state = _uiState.value
        viewModelScope.launch {
            when (state.addressPickerStep) {
                AddressPickerStep.PROVINCE -> {
                    val filtered = getProvincesUseCase(query)
                    _uiState.update { it.copy(provinces = filtered) }
                }
                AddressPickerStep.DISTRICT -> {
                    state.selectedProvince?.let { prov ->
                        val filtered = getDistrictsByProvinceUseCase(prov.id, query)
                        _uiState.update { it.copy(districts = filtered) }
                    }
                }
                AddressPickerStep.WARD -> {
                    val prov = state.selectedProvince
                    val dist = state.selectedDistrict
                    if (prov != null && dist != null) {
                        val filtered = getWardsByDistrictUseCase(prov.id, dist.id, query)
                        _uiState.update { it.copy(wards = filtered) }
                    }
                }
                AddressPickerStep.STREET_DETAIL -> Unit
            }
        }
    }

    private fun loadProvinces() {
        viewModelScope.launch {
            val list = getProvincesUseCase("")
            _uiState.update { it.copy(provinces = list) }
        }
    }

    fun onSelectProvince(province: Province) {
        viewModelScope.launch {
            val districts = getDistrictsByProvinceUseCase(province.id, "")
            _uiState.update {
                it.copy(
                    selectedProvince = province,
                    selectedDistrict = null,
                    selectedWard = null,
                    districts = districts,
                    addressPickerStep = AddressPickerStep.DISTRICT,
                    searchQuery = ""
                )
            }
        }
    }

    fun onSelectDistrict(district: District) {
        viewModelScope.launch {
            val provinceId = _uiState.value.selectedProvince?.id ?: return@launch
            val wards = getWardsByDistrictUseCase(provinceId, district.id, "")
            _uiState.update {
                it.copy(
                    selectedDistrict = district,
                    selectedWard = null,
                    wards = wards,
                    addressPickerStep = AddressPickerStep.WARD,
                    searchQuery = ""
                )
            }
        }
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
        val location = UserLocation(
            latitude = savedAddress.latitude,
            longitude = savedAddress.longitude,
            addressName = savedAddress.addressName,
            fullAddress = savedAddress.fullAddress
        )
        userLocationManager.updateLocation(location)
        viewModelScope.launch { sessionManager.saveUserLocation(location) }
        _uiState.update {
            it.copy(
                userLocation = location,
                displayAddress = savedAddress.addressName,
                showLocationPromptDialog = false
            )
        }
    }

    fun onEditSavedAddress(savedAddress: SavedAddress) {
        viewModelScope.launch {
            val provinces = getProvincesUseCase("")
            val selectedProv = provinces.find { it.name.equals(savedAddress.provinceId, ignoreCase = true) || it.id == savedAddress.provinceId }
                ?: provinces.find { savedAddress.fullAddress.contains(it.name, ignoreCase = true) }

            val districts = if (selectedProv != null) getDistrictsByProvinceUseCase(selectedProv.id, "") else emptyList()
            val selectedDist = districts.find { it.name.equals(savedAddress.districtId, ignoreCase = true) || it.id == savedAddress.districtId }
                ?: districts.find { savedAddress.fullAddress.contains(it.name, ignoreCase = true) }

            val wards = if (selectedProv != null && selectedDist != null) getWardsByDistrictUseCase(selectedProv.id, selectedDist.id, "") else emptyList()
            val selectedW = wards.find { it.name.equals(savedAddress.wardId, ignoreCase = true) || it.id == savedAddress.wardId }
                ?: wards.find { savedAddress.fullAddress.contains(it.name, ignoreCase = true) }

            _uiState.update {
                it.copy(
                    showLocationPromptDialog = false,
                    showAddressPickerSheet = true,
                    editingAddressId = savedAddress.id,
                    provinces = provinces,
                    selectedProvince = selectedProv,
                    districts = districts,
                    selectedDistrict = selectedDist,
                    wards = wards,
                    selectedWard = selectedW,
                    streetDetail = savedAddress.streetDetail.ifBlank { savedAddress.addressName.substringBefore(",") },
                    addressPickerStep = AddressPickerStep.STREET_DETAIL,
                    searchQuery = ""
                )
            }
        }
    }

    fun onDeleteSavedAddress(savedAddress: SavedAddress) {
        viewModelScope.launch { deleteSavedAddressUseCase(savedAddress.id) }
    }

    fun onConfirmCustomAddress() {
        val state = _uiState.value
        val prov = state.selectedProvince?.name ?: ""
        val dist = state.selectedDistrict?.name ?: ""
        val ward = state.selectedWard?.name ?: ""
        val detail = state.streetDetail.trim()

        if (detail.isBlank() || prov.isBlank() || dist.isBlank() || ward.isBlank()) return

        val fullAddress = "$detail, $ward, $dist, $prov"
        val shortAddress = "$detail, $dist"

        viewModelScope.launch {
            val coords = geocodeAddressUseCase(fullAddress)
            val location = UserLocation(
                latitude = coords?.first ?: 20.9626,
                longitude = coords?.second ?: 105.7471,
                addressName = shortAddress,
                fullAddress = fullAddress
            )

            val addressId = state.editingAddressId ?: java.util.UUID.randomUUID().toString()
            val savedAddress = SavedAddress(
                id = addressId,
                addressName = shortAddress,
                fullAddress = fullAddress,
                latitude = location.latitude,
                longitude = location.longitude,
                provinceId = state.selectedProvince?.id ?: "",
                districtId = state.selectedDistrict?.id ?: "",
                wardId = state.selectedWard?.id ?: "",
                streetDetail = detail
            )
            saveAddressUseCase(savedAddress)

            userLocationManager.updateLocation(location)
            sessionManager.saveUserLocation(location)
            _uiState.update {
                it.copy(
                    userLocation = location,
                    displayAddress = shortAddress,
                    showAddressPickerSheet = false,
                    editingAddressId = null
                )
            }
        }
    }
}
