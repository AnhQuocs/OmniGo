package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveRideOffersUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: ObserveRideOffersUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = ObserveRideOffersUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke returns flow of ride offer events from repository`() = runTest {
        val expectedOffer = RideOfferEvent(
            bookingId = 123L,
            pickupLat = 10.7,
            pickupLng = 106.7,
            dropoffLat = 10.8,
            dropoffLng = 106.8,
            distanceInKm = 5.0,
            price = 50000.0,
            status = BookingStatus.PENDING
        )
        every { rideBookingRepository.observeRideOffers() } returns flowOf(expectedOffer)

        val result = useCase().first()

        assertEquals(123L, result.bookingId)
        assertEquals(50000.0, result.price, 0.001)
        assertEquals(BookingStatus.PENDING, result.status)
    }
}
