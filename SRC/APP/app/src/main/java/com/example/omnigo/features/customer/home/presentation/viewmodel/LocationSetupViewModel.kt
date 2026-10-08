package com.example.omnigo.features.customer.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
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

data class LocationSetupUiState(
    val isDetectingGps: Boolean = false,
    val hasPermission: Boolean = false,
    val isGpsOn: Boolean = false,
    val isLocationReady: Boolean = false,
    val showAddressPickerSheet: Boolean = false,
    val addressPickerStep: AddressPickerStep = AddressPickerStep.PROVINCE,
    val provinces: List<Province> = emptyList(),
    val districts: List<District> = emptyList(),
    val wards: List<Ward> = emptyList(),
    val selectedProvince: Province? = null,
    val selectedDistrict: District? = null,
    val selectedWard: Ward? = null,
    val streetDetail: String = "",
    val searchQuery: String = ""
)

sealed interface LocationSetupUiEvent {
    data object NavigateToHome : LocationSetupUiEvent
    data object OpenLocationSettings : LocationSetupUiEvent
    data object RequestLocationPermission : LocationSetupUiEvent
}

@HiltViewModel
class LocationSetupViewModel @Inject constructor(
    private val detectCurrentLocationUseCase: DetectCurrentLocationUseCase,
    private val geocodeAddressUseCase: GeocodeAddressUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val getDistrictsByProvinceUseCase: GetDistrictsByProvinceUseCase,
    private val getWardsByDistrictUseCase: GetWardsByDistrictUseCase,
    private val userLocationManager: UserLocationManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationSetupUiState())
    val uiState: StateFlow<LocationSetupUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<LocationSetupUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkSavedLocation()
        loadProvinces()
    }

    private fun checkSavedLocation() {
        viewModelScope.launch {
            val activeLocation = userLocationManager.currentLocation.value ?: sessionManager.getUserLocation()
            if (activeLocation != null) {
                userLocationManager.updateLocation(activeLocation)
                _uiState.update { it.copy(isLocationReady = true) }
                _uiEvent.send(LocationSetupUiEvent.NavigateToHome)
            }
        }
    }

    private fun loadProvinces() {
        viewModelScope.launch {
            val list = getProvincesUseCase("")
            _uiState.update { it.copy(provinces = list) }
        }
    }

    fun updateHardwareAndPermissionState(hasPermission: Boolean, isGpsOn: Boolean) {
        _uiState.update { it.copy(hasPermission = hasPermission, isGpsOn = isGpsOn) }
        if (hasPermission && isGpsOn && !_uiState.value.isLocationReady && !_uiState.value.isDetectingGps) {
            detectLocation()
        }
    }

    fun onPrimaryButtonClicked(hasPermission: Boolean, isGpsOn: Boolean) {
        viewModelScope.launch {
            if (!hasPermission) {
                _uiEvent.send(LocationSetupUiEvent.RequestLocationPermission)
            } else if (!isGpsOn) {
                _uiEvent.send(LocationSetupUiEvent.OpenLocationSettings)
            } else {
                detectLocation()
            }
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            _uiState.update { it.copy(hasPermission = true) }
            detectLocation()
        }
    }

    fun detectLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingGps = true) }
            when (val result = detectCurrentLocationUseCase()) {
                is DetectLocationResult.Success -> {
                    userLocationManager.updateLocation(result.location)
                    sessionManager.saveUserLocation(result.location)
                    _uiState.update { it.copy(isDetectingGps = false, isLocationReady = true) }
                    _uiEvent.send(LocationSetupUiEvent.NavigateToHome)
                }
                is DetectLocationResult.GpsDisabled -> {
                    _uiState.update { it.copy(isDetectingGps = false, isGpsOn = false) }
                    _uiEvent.send(LocationSetupUiEvent.OpenLocationSettings)
                }
                is DetectLocationResult.PermissionRequired -> {
                    _uiState.update { it.copy(isDetectingGps = false, hasPermission = false) }
                    _uiEvent.send(LocationSetupUiEvent.RequestLocationPermission)
                }
                is DetectLocationResult.LocationUnavailable -> {
                    _uiState.update { it.copy(isDetectingGps = false) }
                }
            }
        }
    }

    fun onOpenManualPickerClicked() {
        _uiState.update {
            it.copy(
                showAddressPickerSheet = true,
                addressPickerStep = AddressPickerStep.PROVINCE,
                searchQuery = ""
            )
        }
        loadProvinces()
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
        val state = _uiState.value
        viewModelScope.launch {
            when (step) {
                AddressPickerStep.PROVINCE -> loadProvinces()
                AddressPickerStep.DISTRICT -> {
                    state.selectedProvince?.let {
                        val dists = getDistrictsByProvinceUseCase(it.id, "")
                        _uiState.update { s -> s.copy(districts = dists) }
                    }
                }
                AddressPickerStep.WARD -> {
                    val prov = state.selectedProvince
                    val dist = state.selectedDistrict
                    if (prov != null && dist != null) {
                        val wards = getWardsByDistrictUseCase(prov.id, dist.id, "")
                        _uiState.update { s -> s.copy(wards = wards) }
                    }
                }
                AddressPickerStep.STREET_DETAIL -> Unit
            }
        }
    }

    fun onConfirmCustomAddress() {
        val state = _uiState.value
        val province = state.selectedProvince?.name ?: ""
        val district = state.selectedDistrict?.name ?: ""
        val ward = state.selectedWard?.name ?: ""
        val street = state.streetDetail.trim()

        val fullAddress = listOf(street, ward, district, province)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        val addressName = if (street.isNotBlank()) street else "$ward, $district"

        viewModelScope.launch {
            val coords = geocodeAddressUseCase(fullAddress)
            val finalLocation = UserLocation(
                latitude = coords?.first ?: 20.9626,
                longitude = coords?.second ?: 105.7471,
                addressName = addressName,
                fullAddress = fullAddress
            )

            userLocationManager.updateLocation(finalLocation)
            sessionManager.saveUserLocation(finalLocation)
            _uiState.update { it.copy(showAddressPickerSheet = false, isLocationReady = true) }
            _uiEvent.send(LocationSetupUiEvent.NavigateToHome)
        }
    }
}
