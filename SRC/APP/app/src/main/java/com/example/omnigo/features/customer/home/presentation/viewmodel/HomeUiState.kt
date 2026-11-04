package com.example.omnigo.features.customer.home.presentation.viewmodel

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.presentation.ui.components.AddressPickerStep
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState

data class HomeUiState(
    val isPopularPlacesLoading: Boolean = false,
    val popularPlaces: List<Restaurant> = emptyList(),
    val filteredPopularPlaces: List<Restaurant> = emptyList(),
    val selectedFilter: HomeFilter = HomeFilter.ALL,
    val vouchers: List<Voucher> = emptyList(),
    val isVouchersLoading: Boolean = false,
    val favoriteRestaurantIds: Set<Long> = emptySet(),
    val hasUnreadNotifications: Boolean = true,
    val showNotificationsSheet: Boolean = false,
    val showQrDialog: Boolean = false,
    val isWalletLoading: Boolean = false,
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
    val cancelTripErrorMessage: String? = null,
    val walletBalanceFormatted: String? = null
)

sealed interface HomeUiEvent {
    data object OpenLocationSettings : HomeUiEvent
    data object OpenAppSettings : HomeUiEvent
    data object RequestLocationPermission : HomeUiEvent
}

