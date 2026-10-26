package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.features.customer.ride.domain.usecase.CalculateRidePricingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.CreateRideBookingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetNearbyDriversUseCase
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import com.example.omnigo.utils.UiText
import io.mockk.coEvery
import io.mockk.coVerify
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
class RideBookingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var calculateRidePricingUseCase: CalculateRidePricingUseCase
    private lateinit var createRideBookingUseCase: CreateRideBookingUseCase
    private lateinit var cancelRideBookingUseCase: CancelRideBookingUseCase
    private lateinit var detectCurrentLocationUseCase: DetectCurrentLocationUseCase
    private lateinit var savedAddressRepository: SavedAddressRepository
    private lateinit var getNearbyDriversUseCase: GetNearbyDriversUseCase
    private lateinit var rideTrackingSessionManager: com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
    private lateinit var ridePlaceSearchCoordinator: RidePlaceSearchCoordinator
    private lateinit var osrmRoutingService: com.example.omnigo.core.location.tracker.OsrmRoutingService
    private val activeRideStateFlow = kotlinx.coroutines.flow.MutableStateFlow<com.example.omnigo.features.customer.ride.domain.model.ActiveRideState?>(null)

    private lateinit var viewModel: RideBookingViewModel

    private val sampleUserLocation = UserLocation(21.0285, 105.8542, "My Home", "123 Trang Tien, Hoan Kiem, Hanoi")
    private val sampleSavedAddress = SavedAddress("addr_1", "Office", "456 Ba Trieu, Hai Ba Trung, Hanoi", 21.0100, 105.8500)
    private val sampleEstimate = PricingEstimate(15000.0, 25000.0, 5000.0, 1.0, null, 45000.0, 3.2, 10, null)
    private val sampleBooking = Booking(999L, 1L, null, null, null, 105.8542, 21.0285, 105.8500, 21.0100, BookingStatus.PENDING, 3.2, 45000.0, "CASH")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        calculateRidePricingUseCase = mockk { coEvery { this@mockk(any(), any(), any(), any(), any(), any()) } returns CalculateRidePricingResult.Success(sampleEstimate) }
        createRideBookingUseCase = mockk()
        cancelRideBookingUseCase = mockk()
        detectCurrentLocationUseCase = mockk { coEvery { this@mockk() } returns DetectLocationResult.Success(sampleUserLocation) }
        savedAddressRepository = mockk { every { getSavedAddresses() } returns flowOf(listOf(sampleSavedAddress)) }
        getNearbyDriversUseCase = mockk(relaxed = true) { coEvery { this@mockk(any(), any(), any()) } returns emptyList() }
        rideTrackingSessionManager = mockk(relaxed = true) { every { activeRideState } returns activeRideStateFlow }
        val searchPlacesUseCase = mockk<com.example.omnigo.features.customer.ride.domain.usecase.SearchPlacesUseCase>(relaxed = true)
        ridePlaceSearchCoordinator = RidePlaceSearchCoordinator(searchPlacesUseCase)
        osrmRoutingService = mockk(relaxed = true) { coEvery { getRouteCoordinates(any(), any(), any(), any()) } returns emptyList() }
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun initViewModel() {
        viewModel = RideBookingViewModel(
            calculateRidePricingUseCase, createRideBookingUseCase, cancelRideBookingUseCase,
            detectCurrentLocationUseCase, savedAddressRepository, mockk(relaxed = true),
            rideTrackingSessionManager, getNearbyDriversUseCase, ridePlaceSearchCoordinator, osrmRoutingService
        )
    }

    @Test
    fun `initialization loads saved addresses and autodetects pickup location`() = runTest {
        initViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.savedAddresses.size)
        assertEquals("Office", state.savedAddresses.first().addressName)
        assertNotNull(state.pickupLocation)
        assertEquals(21.0285, state.pickupLocation!!.latitude, 0.0001)
        assertFalse(state.isLocatingCurrentPosition)
    }

    @Test
    fun `setting dropoff recalculates pricing and updates state with estimate`() = runTest {
        initViewModel()
        advanceUntilIdle()

        val dropoff = LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500)
        viewModel.setDropoffLocation(dropoff)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.pricingEstimate)
        assertEquals(45000.0, state.pricingEstimate!!.totalPrice, 0.001)
        assertNull(state.pricingError)
        assertFalse(state.isCalculatingPrice)
    }

    @Test
    fun `swapping locations flips pickup and dropoff and triggers recalculation`() = runTest {
        initViewModel()
        advanceUntilIdle()

        val dropoff = LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500)
        viewModel.setDropoffLocation(dropoff)
        advanceUntilIdle()

        viewModel.swapLocations()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Office", state.pickupLocation?.name)
        assertEquals("Vị trí hiện tại", state.dropoffLocation?.name)
        assertTrue(state.dropoffLocation?.isCurrentLocation == true)
    }

    @Test
    fun `createBooking success updates currentBooking and enters searching radar state`() = runTest {
        coEvery { createRideBookingUseCase(any(), any(), any(), any(), any(), any(), any()) } returns CreateRideBookingResult.Success(sampleBooking)

        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        viewModel.createBooking()
        testScheduler.advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingBooking)
        assertNotNull(state.currentBooking)
        assertEquals(999L, state.currentBooking!!.bookingId)
        assertTrue(state.isSearchingDriver)
        assertEquals(60, state.searchCountdownSeconds)
    }

    @Test
    fun `createBooking when backend returns CANCELLED displays no driver error and does not enter searching state`() = runTest {
        val cancelledBooking = sampleBooking.copy(status = BookingStatus.CANCELLED)
        coEvery { createRideBookingUseCase(any(), any(), any(), any(), any(), any(), any()) } returns CreateRideBookingResult.Success(cancelledBooking)

        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        viewModel.createBooking()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingBooking)
        assertFalse(state.isSearchingDriver)
        assertNull(state.currentBooking)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage is UiText.StringResource)
    }

    @Test
    fun `createBooking failure displays error message without searching radar`() = runTest {
        coEvery { createRideBookingUseCase(any(), any(), any(), any(), any(), any(), any()) } returns CreateRideBookingResult.Error(
            error = RideError.SERVER_ERROR, message = "Server busy, cannot create booking"
        )

        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        viewModel.createBooking()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingBooking)
        assertFalse(state.isSearchingDriver)
        assertEquals("Server busy, cannot create booking", (state.errorMessage as? UiText.DynamicString)?.value)
    }

    @Test
    fun `cancelBooking success closes radar and clears active booking`() = runTest {
        val cancelledBooking = sampleBooking.copy(status = BookingStatus.CANCELLED)
        coEvery { createRideBookingUseCase(any(), any(), any(), any(), any(), any(), any()) } returns CreateRideBookingResult.Success(sampleBooking)
        coEvery { cancelRideBookingUseCase(999L) } returns CancelRideBookingResult.Success(cancelledBooking)

        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        viewModel.createBooking()
        testScheduler.advanceTimeBy(100)

        assertTrue(viewModel.uiState.value.isSearchingDriver)

        viewModel.cancelBooking()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCancellingBooking)
        assertFalse(state.isSearchingDriver)
        assertNull(state.currentBooking)
        assertNotNull(state.infoMessage)
    }

    @Test
    fun `pricing calculates separate estimates for all vehicle types and updates accurately`() = runTest {
        val bikeEst = sampleEstimate.copy(totalPrice = 25000.0)
        val car4Est = sampleEstimate.copy(totalPrice = 60000.0)
        val car7Est = sampleEstimate.copy(totalPrice = 95000.0)

        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), "BIKE") } returns CalculateRidePricingResult.Success(bikeEst)
        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), "CAR_4_SEAT") } returns CalculateRidePricingResult.Success(car4Est)
        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), "CAR_7_SEAT") } returns CalculateRidePricingResult.Success(car7Est)

        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(25000.0, state.pricingEstimate?.totalPrice ?: 0.0, 0.001)
        assertEquals(25000.0, state.vehicleEstimates[VehicleType.BIKE]?.totalPrice ?: 0.0, 0.001)
        assertEquals(60000.0, state.vehicleEstimates[VehicleType.CAR_4_SEAT]?.totalPrice ?: 0.0, 0.001)
        assertEquals(95000.0, state.vehicleEstimates[VehicleType.CAR_7_SEAT]?.totalPrice ?: 0.0, 0.001)
        assertNull(state.pricingError)

        // Select CAR_7_SEAT -> immediately changes to 95000.0 without fallback to BIKE
        viewModel.selectVehicleType(VehicleType.CAR_7_SEAT)
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(VehicleType.CAR_7_SEAT, updatedState.selectedVehicleType)
        assertEquals(95000.0, updatedState.pricingEstimate?.totalPrice ?: 0.0, 0.001)
        assertEquals(25000.0, updatedState.vehicleEstimates[VehicleType.BIKE]?.totalPrice ?: 0.0, 0.001)
    }

    @Test
    fun `selectVehicleType when vehicle pricing failed sets estimate null and shows error without fallback`() = runTest {
        val bikeEst = sampleEstimate.copy(totalPrice = 25000.0)
        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), "BIKE") } returns CalculateRidePricingResult.Success(bikeEst)
        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), "CAR_4_SEAT") } returns CalculateRidePricingResult.Success(sampleEstimate)
        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), "CAR_7_SEAT") } returns CalculateRidePricingResult.Error(
            RideError.SERVER_ERROR, "No 7-seat cars available"
        )

        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        // Switch to CAR_7_SEAT which failed on backend
        viewModel.selectVehicleType(VehicleType.CAR_7_SEAT)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VehicleType.CAR_7_SEAT, state.selectedVehicleType)
        assertNull(state.pricingEstimate) // MUST be null, NEVER fallback to BIKE
        assertNotNull(state.pricingError)
    }

    @Test
    fun `recalculatePricing displays backend error when all vehicles fail`() = runTest {
        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), any()) } returns CalculateRidePricingResult.Error(
            RideError.SERVER_ERROR, "Server route failed"
        )

        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.pricingEstimate)
        assertTrue(state.vehicleEstimates.isEmpty())
        assertEquals("Server route failed", (state.pricingError as? UiText.DynamicString)?.value)
    }

    @Test
    fun `map pin picking flow opens picker and confirms new pickup location`() = runTest {
        coEvery {
            calculateRidePricingUseCase(any(), any(), any(), any(), any(), any())
        } returns CalculateRidePricingResult.Success(sampleEstimate)

        initViewModel()
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
        initViewModel()
        advanceUntilIdle()

        viewModel.openPlaceSearch(LocationTarget.DROPOFF)
        assertTrue(viewModel.uiState.value.isSearchingPlaces)
        assertEquals(LocationTarget.DROPOFF, viewModel.uiState.value.searchTarget)

        viewModel.closePlaceSearch()
        assertFalse(viewModel.uiState.value.isSearchingPlaces)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `selectSearchResult for dropoff when pickup unconfirmed prompts pickup pin first then dropoff`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.openPlaceSearch(LocationTarget.DROPOFF)
        val selectedDropoff = LocationPoint("Target Place", "123 Target St", 21.05, 105.80)
        viewModel.selectSearchResult(selectedDropoff)

        // Step 1: Must prompt pickup pin first because pickup was not manually pin-confirmed
        val state1 = viewModel.uiState.value
        assertFalse(state1.isSearchingPlaces)
        assertTrue(state1.isMapPinPicking)
        assertEquals(LocationTarget.PICKUP, state1.pinPickingTarget)
        assertEquals(selectedDropoff, state1.pendingDropoffPinLocation)

        // Step 2: Confirm pickup pin -> automatically transitions to dropoff pin picker
        val confirmedPickup = LocationPoint("Confirmed Pickup", "123 Trang Tien", 21.0285, 105.8542)
        viewModel.confirmMapPinLocation(confirmedPickup)

        val state2 = viewModel.uiState.value
        assertTrue(state2.isMapPinPicking)
        assertEquals(LocationTarget.DROPOFF, state2.pinPickingTarget)
        assertEquals(selectedDropoff, state2.pinPickedLocation)
        assertEquals(confirmedPickup, state2.pickupLocation)

        // Step 3: Confirm dropoff pin -> closes pin picking and sets both
        viewModel.confirmMapPinLocation(selectedDropoff)
        val state3 = viewModel.uiState.value
        assertFalse(state3.isMapPinPicking)
        assertEquals(selectedDropoff, state3.dropoffLocation)
    }

    @Test
    fun `selectCurrentLocation sets location and starts map pin picking`() = runTest {
        initViewModel()
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
