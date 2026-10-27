package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.features.customer.ride.domain.usecase.CalculateRidePricingUseCase
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RideBookingPlacesAndPinTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var calculateRidePricingUseCase: CalculateRidePricingUseCase
    private lateinit var detectCurrentLocationUseCase: DetectCurrentLocationUseCase
    private lateinit var savedAddressRepository: SavedAddressRepository
    private lateinit var ridePlaceSearchCoordinator: RidePlaceSearchCoordinator
    private lateinit var osrmRoutingService: com.example.omnigo.core.location.tracker.OsrmRoutingService
    private lateinit var rideTrackingSessionManager: com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
    private val activeRideStateFlow = kotlinx.coroutines.flow.MutableStateFlow<com.example.omnigo.features.customer.ride.domain.model.ActiveRideState?>(null)

    private lateinit var viewModel: RideBookingViewModel

    private val sampleUserLocation = UserLocation(21.0285, 105.8542, "My Home", "123 Trang Tien, Hoan Kiem, Hanoi")
    private val sampleSavedAddress = SavedAddress("addr_1", "Office", "456 Ba Trieu, Hai Ba Trung, Hanoi", 21.0100, 105.8500)
    private val sampleEstimate = PricingEstimate(15000.0, 25000.0, 5000.0, 1.0, null, 45000.0, 3.2, 10, null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        calculateRidePricingUseCase = mockk { coEvery { this@mockk(any(), any(), any(), any(), any(), any()) } returns CalculateRidePricingResult.Success(sampleEstimate) }
        detectCurrentLocationUseCase = mockk { coEvery { this@mockk() } returns DetectLocationResult.Success(sampleUserLocation) }
        savedAddressRepository = mockk { every { getSavedAddresses() } returns flowOf(listOf(sampleSavedAddress)) }
        rideTrackingSessionManager = mockk(relaxed = true) { every { activeRideState } returns activeRideStateFlow }
        val searchPlacesUseCase = mockk<com.example.omnigo.features.customer.ride.domain.usecase.SearchPlacesUseCase>(relaxed = true)
        ridePlaceSearchCoordinator = RidePlaceSearchCoordinator(searchPlacesUseCase)
        osrmRoutingService = mockk(relaxed = true) { coEvery { getRouteCoordinates(any(), any(), any(), any()) } returns emptyList() }

        viewModel = RideBookingViewModel(
            calculateRidePricingUseCase = calculateRidePricingUseCase,
            createRideBookingUseCase = mockk(relaxed = true),
            cancelRideBookingUseCase = mockk(relaxed = true),
            detectCurrentLocationUseCase = detectCurrentLocationUseCase,
            savedAddressRepository = savedAddressRepository,
            rideBookingStatusCoordinator = mockk(relaxed = true),
            rideTrackingSessionManager = rideTrackingSessionManager,
            getNearbyDriversUseCase = mockk(relaxed = true),
            ridePlaceSearchCoordinator = ridePlaceSearchCoordinator,
            osrmRoutingService = osrmRoutingService
        )
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `map pin picking flow opens picker and confirms new pickup location`() = runTest {
        advanceUntilIdle()

        viewModel.startMapPinPicking(LocationTarget.PICKUP)
        assertTrue(viewModel.uiState.value.isMapPinPicking)
        assertEquals(LocationTarget.PICKUP, viewModel.uiState.value.pinPickingTarget)

        val pinnedPoint = LocationPoint(
            name = "Pinned Point",
            address = "123 Pinned Road",
            latitude = 10.77,
            longitude = 106.70
        )
        viewModel.confirmMapPinLocation(pinnedPoint)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isMapPinPicking)
        assertNotNull(state.pickupLocation)
        assertEquals("Pinned Point", state.pickupLocation!!.name)
    }

    @Test
    fun `openPlaceSearch and closePlaceSearch update search state correctly`() = runTest {
        advanceUntilIdle()

        viewModel.openPlaceSearch(LocationTarget.DROPOFF)
        assertTrue(viewModel.uiState.value.isSearchingPlaces)
        assertEquals(LocationTarget.DROPOFF, viewModel.uiState.value.searchTarget)

        viewModel.closePlaceSearch()
        assertFalse(viewModel.uiState.value.isSearchingPlaces)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `selectSearchResult for dropoff when pickup unconfirmed prompts pickup pin then transitions directly to pricing`() = runTest {
        advanceUntilIdle()

        viewModel.openPlaceSearch(LocationTarget.DROPOFF)
        val selectedDropoff = LocationPoint("Target Place", "123 Target St", 21.05, 105.80)
        viewModel.selectSearchResult(selectedDropoff)

        // Step 1: Dropoff is immediately saved, and prompts pickup pin because pickup was unconfirmed
        val state1 = viewModel.uiState.value
        assertFalse(state1.isSearchingPlaces)
        assertEquals(selectedDropoff, state1.dropoffLocation)
        assertTrue(state1.isMapPinPicking)
        assertEquals(LocationTarget.PICKUP, state1.pinPickingTarget)

        // Step 2: Confirm pickup pin -> closes pin picking and calculates pricing directly (NO dropoff pin!)
        val confirmedPickup = LocationPoint("Confirmed Pickup", "123 Trang Tien", 21.0285, 105.8542)
        viewModel.confirmMapPinLocation(confirmedPickup)
        advanceUntilIdle()

        val state2 = viewModel.uiState.value
        assertFalse(state2.isMapPinPicking)
        assertNull(state2.pinPickingTarget)
        assertEquals(confirmedPickup, state2.pickupLocation)
        assertEquals(selectedDropoff, state2.dropoffLocation)
        assertNotNull(state2.pricingEstimate)
    }

    @Test
    fun `selectSearchResult for dropoff when pickup already confirmed transitions directly to pricing`() = runTest {
        advanceUntilIdle()

        // Pickup is already confirmed
        val confirmedPickup = LocationPoint("Confirmed Pickup", "123 Trang Tien", 21.0285, 105.8542)
        viewModel.setPickupLocation(confirmedPickup)
        advanceUntilIdle()

        viewModel.openPlaceSearch(LocationTarget.DROPOFF)
        val selectedDropoff = LocationPoint("Target Place", "123 Target St", 21.05, 105.80)
        viewModel.selectSearchResult(selectedDropoff)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearchingPlaces)
        assertFalse(state.isMapPinPicking)
        assertEquals(confirmedPickup, state.pickupLocation)
        assertEquals(selectedDropoff, state.dropoffLocation)
        assertNotNull(state.pricingEstimate)
    }

    @Test
    fun `selectCurrentLocation sets location and starts map pin picking`() = runTest {
        advanceUntilIdle()

        viewModel.openPlaceSearch(LocationTarget.PICKUP)
        viewModel.selectCurrentLocation("Vị trí hiện tại")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearchingPlaces)
        assertTrue(state.isMapPinPicking)
        assertEquals(LocationTarget.PICKUP, state.pinPickingTarget)
        assertEquals("Vị trí hiện tại", state.pickupLocation?.name)
    }
}
