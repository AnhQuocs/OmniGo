package com.example.omnigo.features.customer.home.presentation.viewmodel

import app.cash.turbine.test
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantsUseCase
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.usecase.DeleteSavedAddressUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
import com.example.omnigo.features.customer.home.domain.usecase.GeocodeAddressUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetDistrictsByProvinceUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetProvincesUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetSavedAddressesUseCase
import com.example.omnigo.features.customer.home.domain.usecase.GetWardsByDistrictUseCase
import com.example.omnigo.features.customer.home.domain.usecase.SaveAddressUseCase
import com.example.omnigo.features.customer.home.presentation.ui.components.AddressPickerStep
import com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getRestaurantsUseCase: GetRestaurantsUseCase
    private lateinit var detectCurrentLocationUseCase: DetectCurrentLocationUseCase
    private lateinit var geocodeAddressUseCase: GeocodeAddressUseCase
    private lateinit var getProvincesUseCase: GetProvincesUseCase
    private lateinit var getDistrictsByProvinceUseCase: GetDistrictsByProvinceUseCase
    private lateinit var getWardsByDistrictUseCase: GetWardsByDistrictUseCase
    private lateinit var getSavedAddressesUseCase: GetSavedAddressesUseCase
    private lateinit var saveAddressUseCase: SaveAddressUseCase
    private lateinit var deleteSavedAddressUseCase: DeleteSavedAddressUseCase
    private lateinit var userLocationManager: com.example.omnigo.core.location.manager.UserLocationManager
    private lateinit var cancelRideBookingUseCase: CancelRideBookingUseCase
    private lateinit var sessionManager: com.example.omnigo.core.datastore.SessionManager
    private lateinit var rideTrackingSessionManager: RideTrackingSessionManager
    private val activeRideStateFlow = MutableStateFlow<ActiveRideState?>(null)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        getRestaurantsUseCase = mockk()
        detectCurrentLocationUseCase = mockk()
        geocodeAddressUseCase = mockk()
        getProvincesUseCase = mockk()
        getDistrictsByProvinceUseCase = mockk()
        getWardsByDistrictUseCase = mockk()
        getSavedAddressesUseCase = mockk()
        saveAddressUseCase = mockk(relaxed = true)
        deleteSavedAddressUseCase = mockk(relaxed = true)
        userLocationManager = com.example.omnigo.core.location.manager.UserLocationManager()
        sessionManager = mockk(relaxed = true)
        cancelRideBookingUseCase = mockk(relaxed = true)

        rideTrackingSessionManager = mockk(relaxed = true)
        every { rideTrackingSessionManager.activeRideState } returns activeRideStateFlow

        coEvery { getRestaurantsUseCase(any()) } returns GetRestaurantsResult.Success(emptyList())
        coEvery { getProvincesUseCase(any()) } returns listOf(Province("01", "Thành phố Hà Nội"))
        coEvery { detectCurrentLocationUseCase() } returns DetectLocationResult.LocationUnavailable
        coEvery { getSavedAddressesUseCase() } returns flowOf(
            listOf(SavedAddress("1", "Nhà", "14D Nguyễn Hy Quang", 21.0, 105.8))
        )

        val coordinator = HomeAddressCoordinator(
            geocodeAddressUseCase,
            getProvincesUseCase,
            getDistrictsByProvinceUseCase,
            getWardsByDistrictUseCase,
            userLocationManager,
            getSavedAddressesUseCase,
            saveAddressUseCase,
            deleteSavedAddressUseCase,
            sessionManager
        )

        viewModel = HomeViewModel(
            getRestaurantsUseCase = getRestaurantsUseCase,
            detectCurrentLocationUseCase = detectCurrentLocationUseCase,
            userLocationManager = userLocationManager,
            sessionManager = sessionManager,
            homeAddressCoordinator = coordinator,
            rideTrackingSessionManager = rideTrackingSessionManager,
            cancelRideBookingUseCase = cancelRideBookingUseCase
        )
    }

    @Test
    fun `initial state loads popular places, provinces and saved addresses`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isPopularPlacesLoading)
        assertEquals(1, state.provinces.size)
        assertEquals("Thành phố Hà Nội", state.provinces[0].name)
        assertEquals(1, state.savedAddresses.size)
        assertEquals("Nhà", state.savedAddresses[0].addressName)
        assertNull(state.activeRideState)
        assertFalse(state.showActiveTripSheet)
    }

    @Test
    fun `onSelectSavedAddress updates location and closes dialog`() = runTest {
        val saved = SavedAddress("1", "Nhà", "14D Nguyễn Hy Quang, Đống Đa", 21.0, 105.8)
        viewModel.onSelectSavedAddress(saved)

        val state = viewModel.uiState.value
        assertEquals("Nhà", state.displayAddress)
        assertFalse(state.showLocationPromptDialog)
        assertEquals(21.0, state.userLocation?.latitude)
    }

    @Test
    fun `onDeleteSavedAddress delegates to deleteSavedAddressUseCase`() = runTest {
        val saved = SavedAddress("1", "Nhà", "14D Nguyễn Hy Quang, Đống Đa", 21.0, 105.8)
        viewModel.onDeleteSavedAddress(saved)
        coVerify { deleteSavedAddressUseCase("1") }
    }

    @Test
    fun `detectLocation when successful updates userLocation and displayAddress`() = runTest {
        val mockLocation = UserLocation(
            latitude = 21.0285,
            longitude = 105.8542,
            addressName = "Quận Hoàn Kiếm, Hà Nội",
            fullAddress = "Phố Tràng Tiền, Quận Hoàn Kiếm, Hà Nội"
        )
        coEvery { detectCurrentLocationUseCase() } returns DetectLocationResult.Success(mockLocation)

        viewModel.detectLocation()

        val state = viewModel.uiState.value
        assertFalse(state.isLocationLoading)
        assertEquals(mockLocation, state.userLocation)
        assertEquals("Quận Hoàn Kiếm, Hà Nội", state.displayAddress)
    }

    @Test
    fun `onEnableGpsClicked without permission emits RequestLocationPermission event`() = runTest {
        viewModel.uiEvent.test {
            viewModel.onEnableGpsClicked(hasPermission = false, isGpsOn = false)

            val event = awaitItem()
            assertTrue(event is HomeUiEvent.RequestLocationPermission)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEnableGpsClicked with permission but GPS off emits OpenLocationSettings event`() = runTest {
        viewModel.uiEvent.test {
            viewModel.onEnableGpsClicked(hasPermission = true, isGpsOn = false)

            val event = awaitItem()
            assertTrue(event is HomeUiEvent.OpenLocationSettings)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `manual address picker flow updates steps, saves address, and confirms address`() = runTest {
        val province = Province("01", "Thành phố Hà Nội")
        val district = District("007", "Quận Hai Bà Trưng")
        val ward = Ward("00277", "Phường Bách Khoa")

        coEvery { getDistrictsByProvinceUseCase("01", "") } returns listOf(district)
        coEvery { getWardsByDistrictUseCase("01", "007", "") } returns listOf(ward)
        coEvery { geocodeAddressUseCase(any()) } returns Pair(21.0065, 105.8431)

        // 1. Open picker
        viewModel.onOpenManualPickerClicked()
        assertEquals(AddressPickerStep.PROVINCE, viewModel.uiState.value.addressPickerStep)
        assertTrue(viewModel.uiState.value.showAddressPickerSheet)

        // 2. Select Province
        viewModel.onSelectProvince(province)
        assertEquals(province, viewModel.uiState.value.selectedProvince)
        assertEquals(AddressPickerStep.DISTRICT, viewModel.uiState.value.addressPickerStep)

        // 3. Select District
        viewModel.onSelectDistrict(district)
        assertEquals(district, viewModel.uiState.value.selectedDistrict)
        assertEquals(AddressPickerStep.WARD, viewModel.uiState.value.addressPickerStep)

        // 4. Select Ward
        viewModel.onSelectWard(ward)
        assertEquals(ward, viewModel.uiState.value.selectedWard)
        assertEquals(AddressPickerStep.STREET_DETAIL, viewModel.uiState.value.addressPickerStep)

        // 5. Enter street details & confirm
        viewModel.onStreetDetailChange("Số 1 Đại Cồ Việt")
        viewModel.onConfirmCustomAddress()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.showAddressPickerSheet)
        assertNotNull(finalState.userLocation)
        assertEquals("Số 1 Đại Cồ Việt, Quận Hai Bà Trưng", finalState.displayAddress)
        coVerify { saveAddressUseCase(any()) }
    }

    @Test
    fun `activeRideState updates uiState and banner can open or close sheet`() = runTest {
        val sampleRide = ActiveRideState(
            bookingId = 101L,
            status = BookingStatus.ACCEPTED,
            price = 45000.0,
            pickupAddress = "14D Nguyễn Hy Quang",
            dropoffAddress = "Đại học Bách Khoa Hà Nội"
        )

        activeRideStateFlow.value = sampleRide

        val stateAfterRide = viewModel.uiState.value
        assertEquals(sampleRide, stateAfterRide.activeRideState)
        assertFalse(stateAfterRide.showActiveTripSheet)

        // Click banner to open sheet
        viewModel.onActiveTripBannerClick()
        assertTrue(viewModel.uiState.value.showActiveTripSheet)

        // Dismiss sheet
        viewModel.dismissActiveTripSheet()
        assertFalse(viewModel.uiState.value.showActiveTripSheet)

        // Re-open then trip finishes -> sheet should auto-close
        viewModel.onActiveTripBannerClick()
        assertTrue(viewModel.uiState.value.showActiveTripSheet)

        activeRideStateFlow.value = sampleRide.copy(status = BookingStatus.COMPLETED)
        assertFalse(viewModel.uiState.value.showActiveTripSheet)
        assertEquals(BookingStatus.COMPLETED, viewModel.uiState.value.activeRideState?.status)
    }

    @Test
    fun `cancel active trip prompt and confirmation flow invokes use case and clears session`() = runTest {
        val sampleRide = ActiveRideState(
            bookingId = 202L,
            status = BookingStatus.ACCEPTED,
            price = 50000.0
        )
        activeRideStateFlow.value = sampleRide
        viewModel.onActiveTripBannerClick()
        assertTrue(viewModel.uiState.value.showActiveTripSheet)

        // 1. Prompt cancel
        viewModel.promptCancelActiveTrip()
        assertTrue(viewModel.uiState.value.showCancelTripConfirmDialog)

        // 2. Dismiss dialog
        viewModel.dismissCancelTripDialog()
        assertFalse(viewModel.uiState.value.showCancelTripConfirmDialog)

        // 3. Confirm cancel
        coEvery { cancelRideBookingUseCase(202L) } returns com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult.Success(
            Booking(
                bookingId = 202L,
                customerId = 1L,
                status = BookingStatus.CANCELLED,
                startLatitude = 0.0,
                startLongitude = 0.0,
                endLatitude = 0.0,
                endLongitude = 0.0,
                price = 50000.0,
                paymentMethod = "CASH"
            )
        )

        viewModel.confirmCancelActiveTrip()

        assertFalse(viewModel.uiState.value.isCancellingActiveTrip)
        assertFalse(viewModel.uiState.value.showCancelTripConfirmDialog)
        assertFalse(viewModel.uiState.value.showActiveTripSheet)
        coVerify { cancelRideBookingUseCase(202L) }
        io.mockk.verify { rideTrackingSessionManager.clearSession() }
    }
}
