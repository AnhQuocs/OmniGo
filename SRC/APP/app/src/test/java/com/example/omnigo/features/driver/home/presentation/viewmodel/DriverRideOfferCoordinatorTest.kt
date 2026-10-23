package com.example.omnigo.features.driver.home.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.AcceptRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import com.example.omnigo.features.driver.domain.usecase.AcceptRideBookingUseCase
import com.example.omnigo.features.driver.domain.usecase.ObserveRideOffersUseCase
import com.example.omnigo.features.driver.domain.usecase.RejectRideBookingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverRideOfferCoordinatorTest {

    private lateinit var observeRideOffersUseCase: ObserveRideOffersUseCase
    private lateinit var acceptRideBookingUseCase: AcceptRideBookingUseCase
    private lateinit var rejectRideBookingUseCase: RejectRideBookingUseCase
    private lateinit var coordinator: DriverRideOfferCoordinator

    private var uiState = DriverHomeUiState()

    @Before
    fun setUp() {
        observeRideOffersUseCase = mockk(relaxed = true)
        acceptRideBookingUseCase = mockk(relaxed = true)
        rejectRideBookingUseCase = mockk(relaxed = true)
        coordinator = DriverRideOfferCoordinator(
            observeRideOffersUseCase = observeRideOffersUseCase,
            acceptRideBookingUseCase = acceptRideBookingUseCase,
            rejectRideBookingUseCase = rejectRideBookingUseCase
        )
        uiState = DriverHomeUiState()
    }

    private fun updateState(transform: (DriverHomeUiState) -> DriverHomeUiState) {
        uiState = transform(uiState)
    }

    private fun sampleOffer(
        bookingId: Long = 101L,
        status: BookingStatus = BookingStatus.PENDING
    ) = RideOfferEvent(
        bookingId = bookingId,
        pickupLat = 10.7,
        pickupLng = 106.7,
        dropoffLat = 10.8,
        dropoffLng = 106.8,
        distanceInKm = 5.0,
        price = 60000.0,
        status = status
    )

    private fun sampleBooking(bookingId: Long = 101L) = Booking(
        bookingId = bookingId,
        customerId = 1L,
        driverId = 2L,
        customerAvatarUrl = null,
        driverAvatarUrl = null,
        startLatitude = 10.7,
        startLongitude = 106.7,
        endLatitude = 10.8,
        endLongitude = 106.8,
        status = BookingStatus.ACCEPTED,
        distanceInKm = 5.0,
        price = 60000.0,
        paymentMethod = "CASH"
    )

    @Test
    fun `handleIncomingOffer ignores offer when driver is offline`() = runTest {
        uiState = uiState.copy(isOnline = false)

        coordinator.handleIncomingOffer(this, sampleOffer(), ::updateState) { uiState }

        assertNull(uiState.currentRideOffer)
    }

    @Test
    fun `handleIncomingOffer displays offer and starts countdown when driver is online`() = runTest {
        uiState = uiState.copy(isOnline = true)

        coordinator.handleIncomingOffer(this, sampleOffer(101L), ::updateState) { uiState }

        assertNotNull(uiState.currentRideOffer)
        assertEquals(101L, uiState.currentRideOffer?.bookingId)
        assertEquals(20, uiState.rideOfferCountdownSeconds)
        coordinator.cancelCountdown()
    }

    @Test
    fun `handleIncomingOffer cancels active offer when CANCELLED event matches current bookingId`() = runTest {
        uiState = uiState.copy(isOnline = true, currentRideOffer = sampleOffer(101L))

        val cancelOffer = sampleOffer(101L, status = BookingStatus.CANCELLED)
        coordinator.handleIncomingOffer(this, cancelOffer, ::updateState) { uiState }

        assertNull(uiState.currentRideOffer)
        assertEquals(DriverRideOfferCoordinator.CANCELLED_OFFER_MESSAGE, uiState.offerActionMessage)
    }

    @Test
    fun `onAcceptOffer calls acceptUseCase and updates acceptedRideBookingId on success`() = runTest {
        val offer = sampleOffer(101L)
        uiState = uiState.copy(isOnline = true, currentRideOffer = offer)
        coEvery { acceptRideBookingUseCase(101L) } returns AcceptRideBookingResult.Success(sampleBooking(101L))

        coordinator.onAcceptOffer(this, 101L, ::updateState) { uiState }
        advanceUntilIdle()

        assertNull(uiState.currentRideOffer)
        assertEquals(101L, uiState.acceptedRideBookingId)
        coVerify(exactly = 1) { acceptRideBookingUseCase(101L) }
    }

    @Test
    fun `onRejectOffer calls rejectUseCase and dismisses offer`() = runTest {
        val offer = sampleOffer(101L)
        uiState = uiState.copy(isOnline = true, currentRideOffer = offer)
        coEvery { rejectRideBookingUseCase(101L) } returns RejectRideBookingResult.Success(
            sampleBooking(101L).copy(status = BookingStatus.CANCELLED)
        )

        coordinator.onRejectOffer(this, 101L, ::updateState) { uiState }
        advanceUntilIdle()

        assertNull(uiState.currentRideOffer)
        coVerify(exactly = 1) { rejectRideBookingUseCase(101L) }
    }

    @Test
    fun `onAcceptOffer blocks duplicate calls when already accepting`() = runTest {
        val offer = sampleOffer(101L)
        uiState = uiState.copy(isOnline = true, currentRideOffer = offer, isRideOfferAccepting = true)

        coordinator.onAcceptOffer(this, 101L, ::updateState) { uiState }

        coVerify(exactly = 0) { acceptRideBookingUseCase(any()) }
    }
}
