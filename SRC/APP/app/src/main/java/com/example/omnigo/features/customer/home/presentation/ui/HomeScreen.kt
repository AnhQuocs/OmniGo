package com.example.omnigo.features.customer.home.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.home.presentation.ui.components.AddressPickerBottomSheet
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeFilterChipsSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeHeaderSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomePopularPlacesSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomePromoBannersSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeQuickWalletSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeSearchSection
import com.example.omnigo.features.customer.home.presentation.ui.components.HomeServicesGridSection
import com.example.omnigo.features.customer.home.presentation.ui.components.LocationPromptDialog
import com.example.omnigo.features.customer.home.presentation.viewmodel.HomeUiEvent
import com.example.omnigo.features.customer.home.presentation.viewmodel.HomeViewModel
import com.example.omnigo.ui.dimens.AppSpacing

private val TOP_BAR_HEIGHT = 60.dp

@Composable
fun HomeScreen(
    onNavigateToService: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectLocation()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.RequestLocationPermission -> {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
                is HomeUiEvent.OpenLocationSettings -> {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }
                is HomeUiEvent.OpenAppSettings -> {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }
            }
        }
    }

    if (uiState.isPopularPlacesLoading && uiState.popularPlaces.isEmpty()) {
        HomeScreenShimmer()
    } else {
        OmniGoMainLayout(
            modifier = modifier,
            headerHeight = TOP_BAR_HEIGHT,
            isRefreshing = uiState.isPopularPlacesLoading,
            onRefresh = viewModel::refresh,
            header = {
                HomeHeaderSection(
                    currentAddress = uiState.displayAddress,
                    isLocating = uiState.isLocationLoading,
                    onLocationClick = viewModel::onLocationHeaderClick,
                    onNotificationClick = onNavigateToNotifications,
                    modifier = Modifier.height(TOP_BAR_HEIGHT)
                )
            }
        ) { contentModifier ->
            val scrollState = rememberScrollState()

            Column(
                modifier = contentModifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(AppSpacing.XS))

                HomeQuickWalletSection()

                Spacer(modifier = Modifier.height(AppSpacing.M))

                HomeSearchSection(
                    onSearchClick = onNavigateToSearch
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                HomeFilterChipsSection()

                Spacer(modifier = Modifier.height(AppSpacing.L))

                HomeServicesGridSection(
                    onServiceClick = onNavigateToService
                )

                Spacer(modifier = Modifier.height(AppSpacing.LPlus))

                HomePromoBannersSection(
                    onBannerClick = { promoCode ->
                        // Handle promo click
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.LPlus))

                HomePopularPlacesSection(
                    popularRestaurants = uiState.popularPlaces,
                    onPlaceClick = { placeId ->
                        // Handle place click
                    },
                    onViewAllClick = {
                        onNavigateToService("food")
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.XXL))
            }
        }
    }

    if (uiState.showLocationPromptDialog) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsOn = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        LocationPromptDialog(
            savedAddresses = uiState.savedAddresses,
            onEnableGpsClick = {
                viewModel.onEnableGpsClicked(hasPermission, isGpsOn)
            },
            onAddNewAddressClick = viewModel::onOpenManualPickerClicked,
            onSelectSavedAddress = viewModel::onSelectSavedAddress,
            onEditSavedAddress = viewModel::onEditSavedAddress,
            onDeleteSavedAddress = viewModel::onDeleteSavedAddress,
            onDismissRequest = viewModel::dismissLocationPrompt
        )
    }

    if (uiState.showAddressPickerSheet) {
        AddressPickerBottomSheet(
            provinces = uiState.provinces,
            districts = uiState.districts,
            wards = uiState.wards,
            selectedProvince = uiState.selectedProvince,
            selectedDistrict = uiState.selectedDistrict,
            selectedWard = uiState.selectedWard,
            streetDetail = uiState.streetDetail,
            currentStep = uiState.addressPickerStep,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onProvinceSelect = viewModel::onSelectProvince,
            onDistrictSelect = viewModel::onSelectDistrict,
            onWardSelect = viewModel::onSelectWard,
            onStreetDetailChange = viewModel::onStreetDetailChange,
            onStepChange = viewModel::onStepChange,
            onConfirmAddress = viewModel::onConfirmCustomAddress,
            onDismissRequest = viewModel::dismissAddressPicker
        )
    }
}