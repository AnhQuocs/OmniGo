package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
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
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
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
    private val sampleBooking = Booking(
        bookingId = 999L,
        customerId = 1L,
        startLongitude = 105.8542,
        startLatitude = 21.0285,
        endLongitude = 105.8500,
        endLatitude = 21.0100,
        status = BookingStatus.PENDING,
        distanceInKm = 3.2,
        price = 45000.0,
        paymentMethod = "CASH"
    )

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
        val ridePricingHelper = RidePricingHelper(calculateRidePricingUseCase, osrmRoutingService)
        viewModel = RideBookingViewModel(
            ridePricingHelper, createRideBookingUseCase, cancelRideBookingUseCase,
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
        assertEquals("Vị trí hiện tại", state.pickupLocation!!.name)
    }

    @Test
    fun `pricing estimate is updated when pickup and dropoff locations are selected`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.pricingEstimate)
        assertEquals(45000.0, state.pricingEstimate!!.totalPrice, 0.0001)
        assertEquals(3.2, state.pricingEstimate!!.distanceInKm, 0.0001)
    }

    @Test
    fun `createBooking transitions state to searching driver`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        coEvery { createRideBookingUseCase(any(), any(), any(), any(), any(), any(), any(), 45000.0) } returns CreateRideBookingResult.Success(sampleBooking)

        viewModel.createBooking()
        testScheduler.runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isSearchingDriver)
        assertEquals(sampleBooking, state.currentBooking)
        assertEquals(60, state.searchCountdownSeconds)
    }

    @Test
    fun `changed price refreshes estimate without starting a ride`() = runTest {
        initViewModel()
        advanceUntilIdle()
        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()
        coEvery { createRideBookingUseCase(any(), any(), any(), any(), any(), any(), any(), 45000.0) } returns
            CreateRideBookingResult.Error(com.example.omnigo.features.customer.ride.domain.error.RideError.SERVER_ERROR, "Price changed")
        coEvery { calculateRidePricingUseCase(any(), any(), any(), any(), any(), any()) } returns
            CalculateRidePricingResult.Success(sampleEstimate.copy(totalPrice = 48000.0))

        viewModel.createBooking()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.currentBooking)
        assertFalse(viewModel.uiState.value.isSearchingDriver)
        assertFalse(viewModel.uiState.value.isCreatingBooking)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals(48000.0, viewModel.uiState.value.pricingEstimate!!.totalPrice, 0.0)
    }

    @Test
    fun `cancelBooking resets searching and booking state`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        coEvery { createRideBookingUseCase(any(), any(), any(), any(), any(), any(), any(), 45000.0) } returns CreateRideBookingResult.Success(sampleBooking)
        coEvery { cancelRideBookingUseCase(sampleBooking.bookingId) } returns CancelRideBookingResult.Success(sampleBooking.copy(status = BookingStatus.CANCELLED))

        viewModel.createBooking()
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isSearchingDriver)

        viewModel.cancelBooking()
        testScheduler.runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isSearchingDriver)
        assertNull(state.currentBooking)
    }

    @Test
    fun `vehicle selection recalculates estimate for the selected type`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        viewModel.selectVehicleType(VehicleType.CAR_4_SEAT)
        advanceUntilIdle()

        assertEquals(VehicleType.CAR_4_SEAT, viewModel.uiState.value.selectedVehicleType)
    }
}
