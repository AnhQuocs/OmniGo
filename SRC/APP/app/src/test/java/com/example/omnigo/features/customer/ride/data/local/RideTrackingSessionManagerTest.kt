package com.example.omnigo.features.customer.ride.data.local

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetCustomerBookingsUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveDriverLiveLocationUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.ObserveRideBookingStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RideTrackingSessionManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var observeRideBookingStatusUseCase: ObserveRideBookingStatusUseCase
    private lateinit var observeDriverLiveLocationUseCase: ObserveDriverLiveLocationUseCase
    private lateinit var getBookingDriverUseCase: GetBookingDriverUseCase
    private lateinit var getCustomerBookingsUseCase: GetCustomerBookingsUseCase
    private lateinit var rideSessionManager: RideSessionManager

    private lateinit var sessionManager: RideTrackingSessionManager

    private val statusFlow = MutableSharedFlow<RideStatusEvent>()
    private val driverLocationFlow = MutableSharedFlow<DriverLiveLocation>()

    private val sampleBooking = Booking(
        bookingId = 100L,
        customerId = 10L,
        status = BookingStatus.PENDING,
        startLatitude = 21.0285,
        startLongitude = 105.8542,
        endLatitude = 21.0333,
        endLongitude = 105.8433,
        price = 45000.0,
        paymentMethod = "CASH"
    )

    private val sampleDriverProfile = BookingDriverProfile(
        driverId = 77L,
        driverName = "Nguyễn Văn A",
        driverPhone = "0987654321",
        vehiclePlate = "29A-12345",
        vehicleType = "BIKE",
        vehicleModel = "Honda Wave",
        avatarUrl = null
    )

    @Before
    fun setUp() {
        observeRideBookingStatusUseCase = mockk()
        observeDriverLiveLocationUseCase = mockk()
        getBookingDriverUseCase = mockk()
        getCustomerBookingsUseCase = mockk()
        rideSessionManager = mockk(relaxed = true)

        every { observeRideBookingStatusUseCase(any()) } returns statusFlow
        every { observeDriverLiveLocationUseCase(any()) } returns driverLocationFlow
        coEvery { getBookingDriverUseCase(any()) } returns GetBookingDriverResult.Success(sampleDriverProfile)

        sessionManager = RideTrackingSessionManager(
            observeRideBookingStatusUseCase = observeRideBookingStatusUseCase,
            observeDriverLiveLocationUseCase = observeDriverLiveLocationUseCase,
            getBookingDriverUseCase = getBookingDriverUseCase,
            getCustomerBookingsUseCase = getCustomerBookingsUseCase,
            rideSessionManager = rideSessionManager,
            scope = testScope
        )
    }

    @After
    fun tearDown() {
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `startSession sets initial active state and saves bookingId to DataStore`() = testScope.runTest {
        sessionManager.startSession(sampleBooking, "Hà Nội", "Hồ Gươm")
        advanceUntilIdle()

        val state = sessionManager.activeRideState.value
        assertNotNull(state)
        assertEquals(100L, state?.bookingId)
        assertEquals(BookingStatus.PENDING, state?.status)
        assertTrue(state?.isPending == true)
        assertTrue(state?.isSearchingDriver == true)
        assertEquals("Hà Nội", state?.pickupAddress)
        assertEquals("Hồ Gươm", state?.dropoffAddress)
        assertEquals(45000.0, state?.price ?: 0.0, 0.01)

        coVerify(exactly = 1) { rideSessionManager.saveActiveBookingId(100L) }
        coVerify(exactly = 1) { observeRideBookingStatusUseCase(100L) }
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `handleStatusEvent ACCEPTED transitions state and subscribes to driver GPS and profile`() = testScope.runTest {
        sessionManager.startSession(sampleBooking)
        advanceUntilIdle()

        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.ACCEPTED, driverId = 77L)
        )
        advanceUntilIdle()

        val state = sessionManager.activeRideState.value
        assertEquals(BookingStatus.ACCEPTED, state?.status)
        assertFalse(state?.isSearchingDriver ?: true)
        assertTrue(state?.isTripActive == true)
        assertEquals(77L, state?.driverProfile?.driverId)
        coVerify(exactly = 1) { observeDriverLiveLocationUseCase(77L) }
        coVerify(exactly = 1) { getBookingDriverUseCase(100L) }
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `handleStatusEvent ARRIVED and IN_PROGRESS update state accordingly`() = testScope.runTest {
        sessionManager.startSession(sampleBooking)
        advanceUntilIdle()

        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.ACCEPTED, driverId = 77L)
        )
        advanceUntilIdle()

        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.ARRIVED, driverId = 77L)
        )
        assertEquals(BookingStatus.ARRIVED, sessionManager.activeRideState.value?.status)

        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.IN_PROGRESS, driverId = 77L)
        )
        assertEquals(BookingStatus.IN_PROGRESS, sessionManager.activeRideState.value?.status)
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `handleStatusEvent PENDING (driver-cancel) clears driver and resets to searching`() = testScope.runTest {
        sessionManager.startSession(sampleBooking)
        advanceUntilIdle()

        // First accepted
        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.ACCEPTED, driverId = 77L)
        )
        advanceUntilIdle()
        assertNotNull(sessionManager.activeRideState.value?.driverProfile)

        // Then driver cancels -> status returns to PENDING
        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.PENDING)
        )
        advanceUntilIdle()

        val state = sessionManager.activeRideState.value
        assertEquals(BookingStatus.PENDING, state?.status)
        assertTrue(state?.isSearchingDriver == true)
        assertNull(state?.driverProfile)
        assertNull(state?.driverLiveLocation)
        assertEquals(RideTrackingSessionManager.STATUS_DRIVER_CANCELLED_MSG, state?.statusMessage)
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `handleStatusEvent COMPLETED clears persistent bookingId and marks trip finished`() = testScope.runTest {
        sessionManager.startSession(sampleBooking)
        advanceUntilIdle()

        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.COMPLETED)
        )
        advanceUntilIdle()

        val state = sessionManager.activeRideState.value
        assertEquals(BookingStatus.COMPLETED, state?.status)
        assertTrue(state?.isFinished == true)
        assertFalse(state?.isSearchingDriver ?: true)
        coVerify(exactly = 1) { rideSessionManager.clearActiveBookingId() }
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `handleDriverLocation calculates distance and eta and updates state`() = testScope.runTest {
        sessionManager.startSession(sampleBooking)
        sessionManager.handleStatusEvent(
            RideStatusEvent(bookingId = 100L, status = BookingStatus.ACCEPTED, driverId = 77L)
        )
        advanceUntilIdle()

        val location = DriverLiveLocation(
            driverId = 77L,
            latitude = 21.0290,
            longitude = 105.8550,
            speed = 30.0f,
            bearing = 45.0f
        )
        sessionManager.handleDriverLocation(location)

        val state = sessionManager.activeRideState.value
        assertNotNull(state?.driverLiveLocation)
        assertEquals(77L, state?.driverLiveLocation?.driverId)
        assertNotNull(state?.liveDistanceKm)
        assertNotNull(state?.liveEtaMinutes)
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `restoreSessionIfAvailable restores active session when saved booking exists in backend`() = testScope.runTest {
        every { rideSessionManager.activeBookingIdFlow } returns flowOf(100L)
        coEvery { getCustomerBookingsUseCase() } returns GetCustomerBookingsResult.Success(
            listOf(
                sampleBooking.copy(status = BookingStatus.ACCEPTED, driverId = 77L)
            )
        )

        sessionManager.restoreSessionIfAvailable()
        advanceUntilIdle()

        val state = sessionManager.activeRideState.value
        assertNotNull(state)
        assertEquals(100L, state?.bookingId)
        assertEquals(BookingStatus.ACCEPTED, state?.status)
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `restoreSessionIfAvailable clears session when saved booking is already COMPLETED`() = testScope.runTest {
        every { rideSessionManager.activeBookingIdFlow } returns flowOf(100L)
        coEvery { getCustomerBookingsUseCase() } returns GetCustomerBookingsResult.Success(
            listOf(
                sampleBooking.copy(status = BookingStatus.COMPLETED)
            )
        )

        sessionManager.restoreSessionIfAvailable()
        advanceUntilIdle()

        assertNull(sessionManager.activeRideState.value)
        coVerify(atLeast = 1) { rideSessionManager.clearActiveBookingId() }
        sessionManager.stopTrackingRealtime()
    }

    @Test
    fun `clearSession cancels tracking and sets active state to null`() = testScope.runTest {
        sessionManager.startSession(sampleBooking)
        advanceUntilIdle()

        sessionManager.clearSession()
        advanceUntilIdle()

        assertNull(sessionManager.activeRideState.value)
        coVerify(atLeast = 1) { rideSessionManager.clearActiveBookingId() }
    }
}
