package com.example.omnigo.features.driver.ride.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.ArriveRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CompleteRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.StartRideBookingResult
import com.example.omnigo.features.driver.domain.usecase.ArrivePickupUseCase
import com.example.omnigo.features.driver.domain.usecase.CompleteTripUseCase
import com.example.omnigo.features.driver.domain.usecase.RejectRideBookingUseCase
import com.example.omnigo.features.driver.domain.usecase.StartTripUseCase
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverActiveTripViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var arrivePickupUseCase: ArrivePickupUseCase
    private lateinit var startTripUseCase: StartTripUseCase
    private lateinit var completeTripUseCase: CompleteTripUseCase
    private lateinit var rejectRideBookingUseCase: RejectRideBookingUseCase
    private lateinit var locationTracker: LocationTracker
    private lateinit var driverTripSessionManager: DriverTripSessionManager
    private lateinit var navigationCoordinator: com.example.omnigo.features.driver.common.presentation.DriverNavigationCoordinator

    private val activeBookingFlow = MutableStateFlow<Booking?>(null)

    private val sampleBooking = Booking(
        bookingId = 555L,
        customerId = 12L,
        driverId = 99L,
        startLatitude = 10.762622,
        startLongitude = 106.660172,
        endLatitude = 10.772622,
        endLongitude = 106.670172,
        status = BookingStatus.ACCEPTED,
        price = 50000.0,
        paymentMethod = "CASH"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        savedStateHandle = SavedStateHandle(
            mapOf(
                "bookingId" to 555L,
                "pickupLat" to "10.762622",
                "pickupLng" to "106.660172",
                "dropoffLat" to "10.772622",
                "dropoffLng" to "106.670172",
                "price" to "50000",
                "paymentMethod" to "CASH"
            )
        )

        arrivePickupUseCase = mockk()
        startTripUseCase = mockk()
        completeTripUseCase = mockk()
        rejectRideBookingUseCase = mockk()
        locationTracker = mockk(relaxed = true)
        driverTripSessionManager = mockk(relaxed = true)
        navigationCoordinator = mockk(relaxed = true)

        every { driverTripSessionManager.activeBooking } returns activeBookingFlow
        every { locationTracker.isLocationPermissionGranted() } returns true
        every { locationTracker.isGpsEnabled() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DriverActiveTripViewModel {
        return DriverActiveTripViewModel(
            savedStateHandle = savedStateHandle,
            arrivePickupUseCase = arrivePickupUseCase,
            startTripUseCase = startTripUseCase,
            completeTripUseCase = completeTripUseCase,
            rejectRideBookingUseCase = rejectRideBookingUseCase,
            locationTracker = locationTracker,
            driverTripSessionManager = driverTripSessionManager,
            navigationCoordinator = navigationCoordinator
        )
    }

    @Test
    fun init_loadsTripDataFromSavedStateHandle() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals(555L, state.bookingId)
        assertEquals(BookingStatus.ACCEPTED, state.status)
        assertEquals(50000.0, state.price, 0.01)
        assertEquals("CASH", state.paymentMethod)
        assertEquals(10000.0, state.platformFee, 0.01)
        assertEquals(40000.0, state.driverEarnings, 0.01)
        assertTrue(state.isCashPayment)
        assertEquals(50000.0, state.amountToCollect, 0.01)
    }

    @Test
    fun updateLocationAndDistance_within50m_setsIsWithinGeofenceTrue() = runTest {
        val viewModel = createViewModel()

        // Point is ~20 meters away from pickup (10.762622, 106.660172)
        val nearLat = 10.762622 + (20.0 / 6371000.0) * (180.0 / Math.PI)
        val nearLng = 106.660172

        viewModel.updateLocationAndDistance(nearLat, nearLng)

        val state = viewModel.uiState.value
        assertNotNull(state.distanceToTargetMeters)
        assertTrue(state.distanceToTargetMeters!! <= 50.0)
        assertTrue(state.isWithinGeofence)
    }

    @Test
    fun updateLocationAndDistance_outside50m_setsIsWithinGeofenceFalse() = runTest {
        val viewModel = createViewModel()

        // Point is ~100 meters away from pickup
        val farLat = 10.762622 + (100.0 / 6371000.0) * (180.0 / Math.PI)
        val farLng = 106.660172

        viewModel.updateLocationAndDistance(farLat, farLng)

        val state = viewModel.uiState.value
        assertNotNull(state.distanceToTargetMeters)
        assertTrue(state.distanceToTargetMeters!! > 50.0)
        assertFalse(state.isWithinGeofence)
    }

    @Test
    fun onArrivePickup_outsideGeofence_showsWarningAndDoesNotCallUseCase() = runTest {
        val viewModel = createViewModel()
        // Far location
        viewModel.updateLocationAndDistance(10.8000, 106.7000)

        viewModel.onArrivePickup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DriverActiveTripViewModel.GEOFENCE_WARNING_PICKUP, state.errorMessage)
        assertTrue(state.showGeofenceOverrideDialog)
        assertEquals(BookingStatus.ACCEPTED, state.status)
        coVerify(exactly = 0) { arrivePickupUseCase(any()) }
    }

    @Test
    fun onConfirmGeofenceOverride_whenAccepted_bypassesGeofenceAndArrivesPickup() = runTest {
        val updatedBooking = sampleBooking.copy(status = BookingStatus.ARRIVED)
        coEvery { arrivePickupUseCase(555L) } returns ArriveRideBookingResult.Success(updatedBooking)

        val viewModel = createViewModel()
        viewModel.updateLocationAndDistance(10.8000, 106.7000)

        viewModel.onArrivePickup()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showGeofenceOverrideDialog)

        viewModel.onConfirmGeofenceOverride()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showGeofenceOverrideDialog)
        assertEquals(BookingStatus.ARRIVED, state.status)
        coVerify(exactly = 1) { arrivePickupUseCase(555L) }
    }

    @Test
    fun onArrivePickup_withinGeofence_success_transitionsToArrived() = runTest {
        coEvery { locationTracker.getCurrentLocation() } returns UserLocation(
            latitude = 10.762622,
            longitude = 106.660172
        )
        val updatedBooking = sampleBooking.copy(status = BookingStatus.ARRIVED)
        coEvery { arrivePickupUseCase(555L) } returns ArriveRideBookingResult.Success(updatedBooking)

        val viewModel = createViewModel()
        viewModel.updateLocationAndDistance(10.762622, 106.660172)

        viewModel.onArrivePickup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BookingStatus.ARRIVED, state.status)
        assertFalse(state.isUpdatingStatus)
        coVerify(exactly = 1) { arrivePickupUseCase(555L) }
        coVerify { driverTripSessionManager.saveActiveTrip(any()) }
    }

    @Test
    fun onStartTrip_success_transitionsToInProgress() = runTest {
        val updatedBooking = sampleBooking.copy(status = BookingStatus.IN_PROGRESS)
        coEvery { startTripUseCase(555L) } returns StartRideBookingResult.Success(updatedBooking)

        val viewModel = createViewModel()
        viewModel.onStartTrip()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BookingStatus.IN_PROGRESS, state.status)
        coVerify(exactly = 1) { startTripUseCase(555L) }
    }

    @Test
    fun onCompleteTrip_outsideGeofence_showsWarning() = runTest {
        val viewModel = createViewModel()
        // Far away from dropoff
        viewModel.updateLocationAndDistance(10.0, 106.0)

        viewModel.onCompleteTrip()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DriverActiveTripViewModel.GEOFENCE_WARNING_DROPOFF, state.errorMessage)
        assertTrue(state.showGeofenceOverrideDialog)
        coVerify(exactly = 0) { completeTripUseCase(any(), any()) }
    }

    @Test
    fun onConfirmGeofenceOverride_whenInProgress_bypassesGeofenceAndCompletesTrip() = runTest {
        val inProgressBooking = sampleBooking.copy(status = BookingStatus.IN_PROGRESS)
        activeBookingFlow.value = inProgressBooking

        val completedBooking = sampleBooking.copy(status = BookingStatus.COMPLETED)
        coEvery { completeTripUseCase(555L, "CASH") } returns CompleteRideBookingResult.Success(completedBooking)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onConfirmGeofenceOverride()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showGeofenceOverrideDialog)
        assertEquals(BookingStatus.COMPLETED, state.status)
        assertTrue(state.showSettlementDialog)
        coVerify(exactly = 1) { completeTripUseCase(555L, "CASH") }
    }

    @Test
    fun onCompleteTrip_withinGeofence_success_showsSettlementAndClearsSession() = runTest {
        coEvery { locationTracker.getCurrentLocation() } returns UserLocation(
            latitude = 10.772622,
            longitude = 106.670172
        )
        val inProgressBooking = sampleBooking.copy(status = BookingStatus.IN_PROGRESS)
        activeBookingFlow.value = inProgressBooking

        val completedBooking = sampleBooking.copy(status = BookingStatus.COMPLETED)
        coEvery { completeTripUseCase(555L, "CASH") } returns CompleteRideBookingResult.Success(completedBooking)

        val viewModel = createViewModel()
        viewModel.updateLocationAndDistance(10.772622, 106.670172)

        viewModel.onCompleteTrip()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BookingStatus.COMPLETED, state.status)
        assertTrue(state.showSettlementDialog)
        coVerify(exactly = 1) { completeTripUseCase(555L, "CASH") }
        coVerify(exactly = 1) { driverTripSessionManager.clearActiveTrip() }
    }

    @Test
    fun onConfirmCancelTrip_success_cancelsTripAndClearsSession() = runTest {
        val cancelledBooking = sampleBooking.copy(status = BookingStatus.CANCELLED)
        coEvery { rejectRideBookingUseCase(555L) } returns RejectRideBookingResult.Success(cancelledBooking)

        val viewModel = createViewModel()
        viewModel.onConfirmCancelTrip()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BookingStatus.CANCELLED, state.status)
        assertTrue(state.isTripCancelled)
        coVerify(exactly = 1) { rejectRideBookingUseCase(555L) }
        coVerify(exactly = 1) { driverTripSessionManager.clearActiveTrip() }
    }
}
