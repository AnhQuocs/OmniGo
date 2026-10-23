package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObserveRideBookingStatusUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: ObserveRideBookingStatusUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = ObserveRideBookingStatusUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke with valid bookingId returns flow from repository`() = runTest {
        val expectedEvent = RideStatusEvent(
            bookingId = 777L,
            status = BookingStatus.ACCEPTED,
            driverId = 888L,
            price = 50000.0
        )
        every { rideBookingRepository.observeBookingStatus(777L) } returns flowOf(expectedEvent)

        val result = useCase(777L).first()

        assertEquals(777L, result.bookingId)
        assertEquals(BookingStatus.ACCEPTED, result.status)
        assertEquals(888L, result.driverId)
    }

    @Test
    fun `invoke with non-positive bookingId returns empty flow`() = runTest {
        val zeroResult = useCase(0L).firstOrNull()
        assertNull(zeroResult)

        val negativeResult = useCase(-5L).firstOrNull()
        assertNull(negativeResult)
    }
}
