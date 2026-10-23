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
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
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

    private lateinit var viewModel: RideBookingViewModel

    private val sampleUserLocation = UserLocation(
        latitude = 21.0285,
        longitude = 105.8542,
        addressName = "My Home",
        fullAddress = "123 Trang Tien, Hoan Kiem, Hanoi"
    )

    private val sampleSavedAddress = SavedAddress(
        id = "addr_1",
        addressName = "Office",
        fullAddress = "456 Ba Trieu, Hai Ba Trung, Hanoi",
        latitude = 21.0100,
        longitude = 105.8500
    )

    private val sampleEstimate = PricingEstimate(
        baseFare = 15000.0,
        distanceFare = 25000.0,
        durationFare = 5000.0,
        surgeMultiplier = 1.0,
        surgeLevel = null,
        totalPrice = 45000.0,
        distanceInKm = 3.2,
        estimatedDurationMinutes = 10,
        h3Index = null
    )

    private val sampleBooking = Booking(
        bookingId = 999L,
        customerId = 1L,
        driverId = null,
        customerAvatarUrl = null,
        driverAvatarUrl = null,
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

        calculateRidePricingUseCase = mockk()
        createRideBookingUseCase = mockk()
        cancelRideBookingUseCase = mockk()
        detectCurrentLocationUseCase = mockk()
        savedAddressRepository = mockk()

        every { savedAddressRepository.getSavedAddresses() } returns flowOf(listOf(sampleSavedAddress))
        coEvery { detectCurrentLocationUseCase() } returns DetectLocationResult.Success(sampleUserLocation)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel() {
        viewModel = RideBookingViewModel(
            calculateRidePricingUseCase = calculateRidePricingUseCase,
            createRideBookingUseCase = createRideBookingUseCase,
            cancelRideBookingUseCase = cancelRideBookingUseCase,
            detectCurrentLocationUseCase = detectCurrentLocationUseCase,
            savedAddressRepository = savedAddressRepository,
            rideBookingStatusCoordinator = mockk(relaxed = true)
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
        coEvery {
            calculateRidePricingUseCase(any(), any(), any(), any(), any())
        } returns CalculateRidePricingResult.Success(sampleEstimate)

        initViewModel()
        advanceUntilIdle()

        val dropoff = LocationPoint(
            name = "Office",
            address = "456 Ba Trieu",
            latitude = 21.0100,
            longitude = 105.8500
        )
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
        coEvery {
            calculateRidePricingUseCase(any(), any(), any(), any(), any())
        } returns CalculateRidePricingResult.Success(sampleEstimate)

        initViewModel()
        advanceUntilIdle()

        val dropoff = LocationPoint(
            name = "Office",
            address = "456 Ba Trieu",
            latitude = 21.0100,
            longitude = 105.8500
        )
        viewModel.setDropoffLocation(dropoff)
        advanceUntilIdle()

        viewModel.swapLocations()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Office", state.pickupLocation?.name)
        assertEquals("My Home", state.dropoffLocation?.name)
    }

    @Test
    fun `createBooking success updates currentBooking and enters searching radar state`() = runTest {
        coEvery {
            calculateRidePricingUseCase(any(), any(), any(), any(), any())
        } returns CalculateRidePricingResult.Success(sampleEstimate)
        coEvery {
            createRideBookingUseCase(any(), any(), any(), any())
        } returns CreateRideBookingResult.Success(sampleBooking)

        initViewModel()
        advanceUntilIdle()

        val dropoff = LocationPoint(
            name = "Office",
            address = "456 Ba Trieu",
            latitude = 21.0100,
            longitude = 105.8500
        )
        viewModel.setDropoffLocation(dropoff)
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
    fun `createBooking failure displays error message without searching radar`() = runTest {
        coEvery {
            calculateRidePricingUseCase(any(), any(), any(), any(), any())
        } returns CalculateRidePricingResult.Success(sampleEstimate)
        coEvery {
            createRideBookingUseCase(any(), any(), any(), any())
        } returns CreateRideBookingResult.Error(
            error = RideError.SERVER_ERROR,
            message = "Server busy, cannot create booking"
        )

        initViewModel()
        advanceUntilIdle()

        val dropoff = LocationPoint(
            name = "Office",
            address = "456 Ba Trieu",
            latitude = 21.0100,
            longitude = 105.8500
        )
        viewModel.setDropoffLocation(dropoff)
        advanceUntilIdle()

        viewModel.createBooking()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingBooking)
        assertFalse(state.isSearchingDriver)
        assertEquals("Server busy, cannot create booking", state.errorMessage)
    }

    @Test
    fun `cancelBooking success closes radar and clears active booking`() = runTest {
        val cancelledBooking = sampleBooking.copy(status = BookingStatus.CANCELLED)

        coEvery {
            calculateRidePricingUseCase(any(), any(), any(), any(), any())
        } returns CalculateRidePricingResult.Success(sampleEstimate)
        coEvery {
            createRideBookingUseCase(any(), any(), any(), any())
        } returns CreateRideBookingResult.Success(sampleBooking)
        coEvery {
            cancelRideBookingUseCase(999L)
        } returns CancelRideBookingResult.Success(cancelledBooking)

        initViewModel()
        advanceUntilIdle()

        val dropoff = LocationPoint(
            name = "Office",
            address = "456 Ba Trieu",
            latitude = 21.0100,
            longitude = 105.8500
        )
        viewModel.setDropoffLocation(dropoff)
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
}
