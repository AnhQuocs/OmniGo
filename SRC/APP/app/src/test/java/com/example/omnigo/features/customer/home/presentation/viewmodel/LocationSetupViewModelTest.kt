package com.example.omnigo.features.customer.home.presentation.viewmodel

import app.cash.turbine.test
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
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var detectCurrentLocationUseCase: DetectCurrentLocationUseCase
    private lateinit var geocodeAddressUseCase: GeocodeAddressUseCase
    private lateinit var getProvincesUseCase: GetProvincesUseCase
    private lateinit var getDistrictsByProvinceUseCase: GetDistrictsByProvinceUseCase
    private lateinit var getWardsByDistrictUseCase: GetWardsByDistrictUseCase
    private lateinit var userLocationManager: UserLocationManager
    private lateinit var sessionManager: SessionManager

    private lateinit var viewModel: LocationSetupViewModel

    @Before
    fun setUp() {
        detectCurrentLocationUseCase = mockk()
        geocodeAddressUseCase = mockk()
        getProvincesUseCase = mockk()
        getDistrictsByProvinceUseCase = mockk()
        getWardsByDistrictUseCase = mockk()
        userLocationManager = UserLocationManager()
        sessionManager = mockk(relaxed = true)

        coEvery { getProvincesUseCase(any()) } returns listOf(Province("01", "Thành phố Hà Nội"))
        coEvery { sessionManager.getUserLocation() } returns null

        viewModel = LocationSetupViewModel(
            detectCurrentLocationUseCase,
            geocodeAddressUseCase,
            getProvincesUseCase,
            getDistrictsByProvinceUseCase,
            getWardsByDistrictUseCase,
            userLocationManager,
            sessionManager
        )
    }

    @Test
    fun `initial state loads provinces`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(1, state.provinces.size)
        assertEquals("Thành phố Hà Nội", state.provinces[0].name)
    }

    @Test
    fun `onPrimaryButtonClicked without permission emits RequestLocationPermission`() = runTest {
        viewModel.uiEvent.test {
            viewModel.onPrimaryButtonClicked(hasPermission = false, isGpsOn = true)
            val event = awaitItem()
            assertTrue(event is LocationSetupUiEvent.RequestLocationPermission)
        }
    }

    @Test
    fun `onPrimaryButtonClicked with permission but gps off emits OpenLocationSettings`() = runTest {
        viewModel.uiEvent.test {
            viewModel.onPrimaryButtonClicked(hasPermission = true, isGpsOn = false)
            val event = awaitItem()
            assertTrue(event is LocationSetupUiEvent.OpenLocationSettings)
        }
    }

    @Test
    fun `detectLocation success updates userLocationManager, saves to session and emits NavigateToHome`() = runTest {
        val expectedLoc = UserLocation(
            latitude = 21.0,
            longitude = 105.8,
            addressName = "Hà Nội",
            fullAddress = "Hà Nội, Việt Nam"
        )
        coEvery { detectCurrentLocationUseCase() } returns DetectLocationResult.Success(expectedLoc)

        viewModel.uiEvent.test {
            viewModel.detectLocation()
            val event = awaitItem()
            assertTrue(event is LocationSetupUiEvent.NavigateToHome)
            assertEquals(expectedLoc, userLocationManager.currentLocation.value)
            coVerify { sessionManager.saveUserLocation(expectedLoc) }
        }
    }

    @Test
    fun `onSelectProvince, district, ward transitions steps correctly`() = runTest {
        val prov = Province("01", "Hà Nội")
        val dist = District("001", "Ba Đình")
        val ward = Ward("00001", "Phúc Xá", "Phường")

        coEvery { getDistrictsByProvinceUseCase("01", "") } returns listOf(dist)
        coEvery { getWardsByDistrictUseCase("01", "001", "") } returns listOf(ward)

        viewModel.onSelectProvince(prov)
        assertEquals(prov, viewModel.uiState.value.selectedProvince)
        assertEquals(AddressPickerStep.DISTRICT, viewModel.uiState.value.addressPickerStep)
        assertEquals(1, viewModel.uiState.value.districts.size)

        viewModel.onSelectDistrict(dist)
        assertEquals(dist, viewModel.uiState.value.selectedDistrict)
        assertEquals(AddressPickerStep.WARD, viewModel.uiState.value.addressPickerStep)
        assertEquals(1, viewModel.uiState.value.wards.size)

        viewModel.onSelectWard(ward)
        assertEquals(ward, viewModel.uiState.value.selectedWard)
        assertEquals(AddressPickerStep.STREET_DETAIL, viewModel.uiState.value.addressPickerStep)
    }

    @Test
    fun `onConfirmCustomAddress geocodes and updates userLocationManager and saves session`() = runTest {
        val prov = Province("01", "Hà Nội")
        val dist = District("001", "Ba Đình")
        val ward = Ward("00001", "Phúc Xá", "Phường")

        coEvery { getDistrictsByProvinceUseCase("01", "") } returns listOf(dist)
        coEvery { getWardsByDistrictUseCase("01", "001", "") } returns listOf(ward)
        coEvery { geocodeAddressUseCase(any()) } returns Pair(21.03, 105.84)

        viewModel.onSelectProvince(prov)
        viewModel.onSelectDistrict(dist)
        viewModel.onSelectWard(ward)
        viewModel.onStreetDetailChange("Số 10")

        viewModel.uiEvent.test {
            viewModel.onConfirmCustomAddress()
            val event = awaitItem()
            assertTrue(event is LocationSetupUiEvent.NavigateToHome)
            assertNotNull(userLocationManager.currentLocation.value)
            assertEquals("Số 10", userLocationManager.currentLocation.value?.addressName)
            coVerify { sessionManager.saveUserLocation(any()) }
        }
    }
}
