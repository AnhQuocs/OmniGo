package com.example.omnigo.features.customer.home.presentation.viewmodel

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.usecase.DeleteSavedAddressUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GeocodeAddressUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetDistrictsByProvinceUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetProvincesUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetSavedAddressesUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetWardsByDistrictUseCase
import com.example.omnigo.features.customer.home.domain.usecase.SaveAddressUseCase
import com.example.omnigo.features.customer.home.presentation.ui.components.AddressPickerStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class HomeAddressCoordinator @Inject constructor(
    private val geocodeAddressUseCase: GeocodeAddressUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val getDistrictsByProvinceUseCase: GetDistrictsByProvinceUseCase,
    private val getWardsByDistrictUseCase: GetWardsByDistrictUseCase,
    private val userLocationManager: UserLocationManager,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val saveAddressUseCase: SaveAddressUseCase,
    private val deleteSavedAddressUseCase: DeleteSavedAddressUseCase,
    private val sessionManager: SessionManager
) {
    fun getSavedAddresses(): Flow<List<SavedAddress>> = getSavedAddressesUseCase()

    fun loadProvinces(scope: CoroutineScope, updateState: ((HomeUiState) -> HomeUiState) -> Unit) {
        scope.launch {
            val list = getProvincesUseCase("")
            updateState { it.copy(provinces = list) }
        }
    }

    fun onSearchQueryChange(
        scope: CoroutineScope,
        query: String,
        currentState: HomeUiState,
        updateState: ((HomeUiState) -> HomeUiState) -> Unit
    ) {
        updateState { it.copy(searchQuery = query) }
        scope.launch {
            when (currentState.addressPickerStep) {
                AddressPickerStep.PROVINCE -> {
                    val filtered = getProvincesUseCase(query)
                    updateState { it.copy(provinces = filtered) }
                }
                AddressPickerStep.DISTRICT -> {
                    currentState.selectedProvince?.let { prov ->
                        val filtered = getDistrictsByProvinceUseCase(prov.id, query)
                        updateState { it.copy(districts = filtered) }
                    }
                }
                AddressPickerStep.WARD -> {
                    val prov = currentState.selectedProvince
                    val dist = currentState.selectedDistrict
                    if (prov != null && dist != null) {
                        val filtered = getWardsByDistrictUseCase(prov.id, dist.id, query)
                        updateState { it.copy(wards = filtered) }
                    }
                }
                AddressPickerStep.STREET_DETAIL -> Unit
            }
        }
    }

    fun onSelectProvince(
        scope: CoroutineScope,
        province: Province,
        updateState: ((HomeUiState) -> HomeUiState) -> Unit
    ) {
        scope.launch {
            val districts = getDistrictsByProvinceUseCase(province.id, "")
            updateState {
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

    fun onSelectDistrict(
        scope: CoroutineScope,
        provinceId: String,
        district: District,
        updateState: ((HomeUiState) -> HomeUiState) -> Unit
    ) {
        scope.launch {
            val wards = getWardsByDistrictUseCase(provinceId, district.id, "")
            updateState {
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

    fun onSelectSavedAddress(
        scope: CoroutineScope,
        savedAddress: SavedAddress,
        updateState: ((HomeUiState) -> HomeUiState) -> Unit
    ) {
        val location = UserLocation(
            latitude = savedAddress.latitude,
            longitude = savedAddress.longitude,
            addressName = savedAddress.addressName,
            fullAddress = savedAddress.fullAddress
        )
        userLocationManager.updateLocation(location)
        scope.launch { sessionManager.saveUserLocation(location) }
        updateState {
            it.copy(
                userLocation = location,
                displayAddress = savedAddress.addressName,
                showLocationPromptDialog = false
            )
        }
    }

    fun onEditSavedAddress(
        scope: CoroutineScope,
        savedAddress: SavedAddress,
        updateState: ((HomeUiState) -> HomeUiState) -> Unit
    ) {
        scope.launch {
            val provinces = getProvincesUseCase("")
            val selectedProv = provinces.find {
                it.name.equals(savedAddress.provinceId, ignoreCase = true) ||
                        it.id == savedAddress.provinceId ||
                        savedAddress.fullAddress.contains(it.name, ignoreCase = true)
            }
            val districts = if (selectedProv != null) getDistrictsByProvinceUseCase(selectedProv.id, "") else emptyList()
            val selectedDist = districts.find {
                it.name.equals(savedAddress.districtId, ignoreCase = true) ||
                        it.id == savedAddress.districtId ||
                        savedAddress.fullAddress.contains(it.name, ignoreCase = true)
            }
            val wards = if (selectedProv != null && selectedDist != null) {
                getWardsByDistrictUseCase(selectedProv.id, selectedDist.id, "")
            } else emptyList()
            val selectedW = wards.find {
                it.name.equals(savedAddress.wardId, ignoreCase = true) ||
                        it.id == savedAddress.wardId ||
                        savedAddress.fullAddress.contains(it.name, ignoreCase = true)
            }

            updateState {
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

    fun onDeleteSavedAddress(scope: CoroutineScope, savedAddress: SavedAddress) {
        scope.launch { deleteSavedAddressUseCase(savedAddress.id) }
    }

    fun onConfirmCustomAddress(
        scope: CoroutineScope,
        currentState: HomeUiState,
        updateState: ((HomeUiState) -> HomeUiState) -> Unit
    ) {
        val prov = currentState.selectedProvince?.name ?: ""
        val dist = currentState.selectedDistrict?.name ?: ""
        val ward = currentState.selectedWard?.name ?: ""
        val detail = currentState.streetDetail.trim()

        if (detail.isBlank() || prov.isBlank() || dist.isBlank() || ward.isBlank()) return

        val fullAddress = "$detail, $ward, $dist, $prov"
        val shortAddress = "$detail, $dist"

        scope.launch {
            val coords = geocodeAddressUseCase(fullAddress)
            val location = UserLocation(
                latitude = coords?.first ?: 20.9626,
                longitude = coords?.second ?: 105.7471,
                addressName = shortAddress,
                fullAddress = fullAddress
            )

            val addressId = currentState.editingAddressId ?: UUID.randomUUID().toString()
            val savedAddress = SavedAddress(
                id = addressId,
                addressName = shortAddress,
                fullAddress = fullAddress,
                latitude = location.latitude,
                longitude = location.longitude,
                provinceId = currentState.selectedProvince?.id ?: "",
                districtId = currentState.selectedDistrict?.id ?: "",
                wardId = currentState.selectedWard?.id ?: "",
                streetDetail = detail
            )
            saveAddressUseCase(savedAddress)

            userLocationManager.updateLocation(location)
            sessionManager.saveUserLocation(location)
            updateState {
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
