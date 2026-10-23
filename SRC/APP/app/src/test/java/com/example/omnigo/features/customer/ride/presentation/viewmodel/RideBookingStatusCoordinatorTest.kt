package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.features.customer.ride.data.local.RideSessionManager
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveRideBookingStatusUseCase
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RideBookingStatusCoordinatorTest {

    private lateinit var observeRideBookingStatusUseCase: ObserveRideBookingStatusUseCase
    private lateinit var getBookingDriverUseCase: GetBookingDriverUseCase
    private lateinit var rideSessionManager: RideSessionManager
    private lateinit var coordinator: RideBookingStatusCoordinator

    private var uiState = RideBookingUiState()

    private val sampleBooking = Booking(
        bookingId = 100L,
        customerId = 1L,
        driverId = null,
        customerAvatarUrl = null,
        driverAvatarUrl = null,
        startLongitude = 105.85,
        startLatitude = 21.02,
        endLongitude = 105.86,
        endLatitude = 21.03,
        status = BookingStatus.PENDING,
        distanceInKm = 2.0,
        price = 30000.0,
        paymentMethod = "CASH"
    )

    private val sampleDriver = BookingDriverProfile(
        driverId = 200L,
        driverName = "Nguyen Van A",
        driverPhone = "0901234567",
        vehiclePlate = "29A-12345",
        vehicleType = "CAR_4",
        vehicleModel = "Toyota Vios",
        avatarUrl = null
    )

    @Before
    fun setUp() {
        observeRideBookingStatusUseCase = mockk(relaxed = true)
        getBookingDriverUseCase = mockk(relaxed = true)
        rideSessionManager = mockk(relaxed = true)

        coordinator = RideBookingStatusCoordinator(
            observeRideBookingStatusUseCase = observeRideBookingStatusUseCase,
            getBookingDriverUseCase = getBookingDriverUseCase,
            rideSessionManager = rideSessionManager
        )
        uiState = RideBookingUiState(currentBooking = sampleBooking, isSearchingDriver = true)
    }

    private fun updateState(transform: (RideBookingUiState) -> RideBookingUiState) {
        uiState = transform(uiState)
    }

    @Test
    fun `handleStatusEvent with ACCEPTED updates booking, fetches driver and dismisses radar`() = runTest {
        coEvery { getBookingDriverUseCase(100L) } returns GetBookingDriverResult.Success(sampleDriver)

        val acceptedEvent = RideStatusEvent(
            bookingId = 100L,
            status = BookingStatus.ACCEPTED,
            driverId = 200L
        )

        coordinator.handleStatusEvent(this, acceptedEvent, ::updateState) { uiState }
        advanceUntilIdle()

        assertFalse(uiState.isSearchingDriver)
        assertEquals(BookingStatus.ACCEPTED, uiState.currentBooking?.status)
        assertEquals(200L, uiState.currentBooking?.driverId)
        assertNotNull(uiState.assignedDriver)
        assertEquals("Nguyen Van A", uiState.assignedDriver?.driverName)
        coVerify(exactly = 1) { getBookingDriverUseCase(100L) }
    }

    @Test
    fun `handleStatusEvent with ARRIVED updates trip status to ARRIVED`() = runTest {
        uiState = uiState.copy(currentBooking = sampleBooking.copy(status = BookingStatus.ACCEPTED))

        val arrivedEvent = RideStatusEvent(
            bookingId = 100L,
            status = BookingStatus.ARRIVED
        )

        coordinator.handleStatusEvent(this, arrivedEvent, ::updateState) { uiState }

        assertEquals(BookingStatus.ARRIVED, uiState.currentBooking?.status)
    }

    @Test
    fun `handleStatusEvent with IN_PROGRESS updates trip status to IN_PROGRESS`() = runTest {
        uiState = uiState.copy(currentBooking = sampleBooking.copy(status = BookingStatus.ARRIVED))

        val inProgressEvent = RideStatusEvent(
            bookingId = 100L,
            status = BookingStatus.IN_PROGRESS
        )

        coordinator.handleStatusEvent(this, inProgressEvent, ::updateState) { uiState }

        assertEquals(BookingStatus.IN_PROGRESS, uiState.currentBooking?.status)
    }

    @Test
    fun `handleStatusEvent with COMPLETED updates status and clears session`() = runTest {
        uiState = uiState.copy(currentBooking = sampleBooking.copy(status = BookingStatus.IN_PROGRESS))

        val completedEvent = RideStatusEvent(
            bookingId = 100L,
            status = BookingStatus.COMPLETED,
            price = 32000.0
        )

        coordinator.handleStatusEvent(this, completedEvent, ::updateState) { uiState }
        advanceUntilIdle()

        assertEquals(BookingStatus.COMPLETED, uiState.currentBooking?.status)
        assertEquals(32000.0, uiState.currentBooking?.price ?: 0.0, 0.001)
        coVerify(exactly = 1) { rideSessionManager.clearActiveBookingId() }
    }

    @Test
    fun `handleStatusEvent with CANCELLED resets booking, driver and clears session`() = runTest {
        uiState = uiState.copy(
            currentBooking = sampleBooking.copy(status = BookingStatus.ACCEPTED),
            assignedDriver = sampleDriver
        )

        val cancelledEvent = RideStatusEvent(
            bookingId = 100L,
            status = BookingStatus.CANCELLED
        )

        coordinator.handleStatusEvent(this, cancelledEvent, ::updateState) { uiState }
        advanceUntilIdle()

        assertNull(uiState.currentBooking)
        assertNull(uiState.assignedDriver)
        assertFalse(uiState.isSearchingDriver)
        coVerify(exactly = 1) { rideSessionManager.clearActiveBookingId() }
    }

    @Test
    fun `handleStatusEvent ignores events with mismatched bookingId`() = runTest {
        val foreignEvent = RideStatusEvent(
            bookingId = 99999L,
            status = BookingStatus.ACCEPTED,
            driverId = 300L
        )

        coordinator.handleStatusEvent(this, foreignEvent, ::updateState) { uiState }

        assertEquals(BookingStatus.PENDING, uiState.currentBooking?.status)
        assertNull(uiState.assignedDriver)
        coVerify(exactly = 0) { getBookingDriverUseCase(any()) }
    }
}
