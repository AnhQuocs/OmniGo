package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.R
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import com.example.omnigo.features.customer.home.domain.usecase.DetectCurrentLocationUseCase
import com.example.omnigo.features.customer.home.domain.usecase.DetectLocationResult
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.features.customer.ride.domain.usecase.CalculateRidePricingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.CreateRideBookingUseCase
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.utils.UiText
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Guards against empty toasts: a blank backend message must resolve to a localized default. */
@OptIn(ExperimentalCoroutinesApi::class)
class RideBookingErrorMessageTest {

    private val testDispatcher = StandardTestDispatcher()
    private val createUseCase = mockk<CreateRideBookingUseCase>()
    private val cancelUseCase = mockk<CancelRideBookingUseCase>()
    private lateinit var viewModel: RideBookingViewModel

    private val estimate = PricingEstimate(15000.0, 25000.0, 5000.0, 1.0, null, 45000.0, 3.2, 10, null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val pricing = mockk<CalculateRidePricingUseCase> {
            coEvery { this@mockk(any(), any(), any(), any(), any(), any()) } returns CalculateRidePricingResult.Success(estimate)
        }
        val detect = mockk<DetectCurrentLocationUseCase> {
            coEvery { this@mockk() } returns DetectLocationResult.Success(UserLocation(21.0285, 105.8542, "Home", "Hanoi"))
        }
        val savedAddresses = mockk<SavedAddressRepository> { every { getSavedAddresses() } returns flowOf(emptyList()) }
        val session = mockk<com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager>(relaxed = true) {
            every { activeRideState } returns MutableStateFlow(null)
        }
        val osrm = mockk<com.example.omnigo.core.location.tracker.OsrmRoutingService>(relaxed = true) {
            coEvery { getRouteCoordinates(any(), any(), any(), any(), any()) } returns emptyList()
        }
        viewModel = RideBookingViewModel(
            calculateRidePricingUseCase = pricing,
            createRideBookingUseCase = createUseCase,
            cancelRideBookingUseCase = cancelUseCase,
            detectCurrentLocationUseCase = detect,
            savedAddressRepository = savedAddresses,
            rideBookingStatusCoordinator = mockk(relaxed = true),
            rideTrackingSessionManager = session,
            getNearbyDriversUseCase = mockk(relaxed = true),
            ridePlaceSearchCoordinator = RidePlaceSearchCoordinator(mockk(relaxed = true)),
            osrmRoutingService = osrm
        )
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `createBooking failure with blank message falls back to localized default`() = runTest {
        coEvery { createUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            CreateRideBookingResult.Error(error = RideError.SERVER_ERROR, message = "")
        advanceUntilIdle()
        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        viewModel.createBooking()
        advanceUntilIdle()

        val error = viewModel.uiState.value.errorMessage
        assertTrue(error is UiText.StringResource)
        assertEquals(R.string.ride_error_create_failed, (error as UiText.StringResource).resId)
    }

    @Test
    fun `createBooking failure with backend message shows that message`() = runTest {
        coEvery { createUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            CreateRideBookingResult.Error(error = RideError.SERVER_ERROR, message = "Customer already has an active booking")
        advanceUntilIdle()
        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()

        viewModel.createBooking()
        advanceUntilIdle()

        val error = viewModel.uiState.value.errorMessage as UiText.DynamicString
        assertEquals("Customer already has an active booking", error.value)
    }

    @Test
    fun `cancelBooking failure with blank message falls back to localized default`() = runTest {
        coEvery { createUseCase(any(), any(), any(), any(), any(), any(), any()) } returns CreateRideBookingResult.Success(
            com.example.omnigo.features.customer.ride.domain.model.Booking(bookingId = 7L)
        )
        coEvery { cancelUseCase(7L) } returns CancelRideBookingResult.Error(error = RideError.SERVER_ERROR, message = " ")
        advanceUntilIdle()
        viewModel.setDropoffLocation(LocationPoint("Office", "456 Ba Trieu", 21.0100, 105.8500))
        advanceUntilIdle()
        viewModel.createBooking()
        advanceUntilIdle()

        viewModel.cancelBooking()
        advanceUntilIdle()

        val error = viewModel.uiState.value.errorMessage
        assertTrue(error is UiText.StringResource)
        assertEquals(R.string.ride_error_cancel_failed, (error as UiText.StringResource).resId)
    }
}
