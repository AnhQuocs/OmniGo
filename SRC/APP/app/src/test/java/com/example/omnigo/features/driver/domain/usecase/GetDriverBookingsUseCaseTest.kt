package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetDriverBookingsResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import com.example.omnigo.features.driver.earnings.data.remote.api.DriverPaymentApi
import com.example.omnigo.features.driver.earnings.data.remote.dto.TransactionResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDriverBookingsUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var paymentApi: DriverPaymentApi
    private lateinit var useCase: GetDriverBookingsUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        paymentApi = mockk()
        useCase = GetDriverBookingsUseCase(rideBookingRepository, paymentApi)
    }

    @Test
    fun `invoke enriches bookings with actual platform fee from transactions without dividing by 0_20`() = runTest {
        val booking = Booking(
            bookingId = 101L,
            price = 100000.0,
            status = BookingStatus.COMPLETED
        )
        coEvery { rideBookingRepository.getDriverBookings() } returns GetDriverBookingsResult.Success(listOf(booking))
        coEvery { paymentApi.getTransactions() } returns listOf(
            TransactionResponseDto(
                bookingId = 101L,
                transactionType = "COMMISSION_FEE",
                amount = -15000.0 // Phí sàn thực tế 15,000đ (không phải cố định 20%)
            )
        )

        val result = useCase()

        assertTrue(result is GetDriverBookingsResult.Success)
        val success = result as GetDriverBookingsResult.Success
        assertEquals(1, success.bookings.size)
        val enriched = success.bookings[0]

        // Phí thực tế từ transaction
        assertEquals(15000.0, enriched.actualPlatformFee, 0.01)
        // Thu nhập thực tế = 100,000 - 15,000 = 85,000đ
        assertEquals(85000.0, enriched.actualEarnings, 0.01)
    }

    @Test
    fun `invoke preserves API platform fee if already present in booking`() = runTest {
        val booking = Booking(
            bookingId = 102L,
            price = 200000.0,
            platformFee = 30000.0,
            status = BookingStatus.COMPLETED
        )
        coEvery { rideBookingRepository.getDriverBookings() } returns GetDriverBookingsResult.Success(listOf(booking))
        coEvery { paymentApi.getTransactions() } returns emptyList()

        val result = useCase()

        assertTrue(result is GetDriverBookingsResult.Success)
        val success = result as GetDriverBookingsResult.Success
        val enriched = success.bookings[0]
        assertEquals(30000.0, enriched.actualPlatformFee, 0.01)
        assertEquals(170000.0, enriched.actualEarnings, 0.01)
    }
}

