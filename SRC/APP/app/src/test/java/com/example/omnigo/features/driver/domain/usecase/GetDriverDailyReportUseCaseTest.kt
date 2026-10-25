package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDriverDailyReportUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: GetDriverDailyReportUseCase

    private val sampleReport = DriverDailyReport(
        totalTrips = 8,
        totalRevenue = 400000.0,
        driverEarnings = 320000.0,
        platformFee = 80000.0,
        cashCollected = 250000.0,
        appPaid = 150000.0
    )

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = GetDriverDailyReportUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke delegates to repository and returns success with daily report`() = runTest {
        coEvery {
            rideBookingRepository.getDriverDailyReport()
        } returns GetDriverDailyReportResult.Success(sampleReport)

        val result = useCase()

        assertTrue(result is GetDriverDailyReportResult.Success)
        val success = result as GetDriverDailyReportResult.Success
        assertEquals(8, success.report.totalTrips)
        assertEquals(400000.0, success.report.totalRevenue, 0.01)
        assertEquals(320000.0, success.report.driverEarnings, 0.01)
        assertEquals(80000.0, success.report.platformFee, 0.01)
        coVerify(exactly = 1) { rideBookingRepository.getDriverDailyReport() }
    }

    @Test
    fun `invoke when repository returns error propagates error`() = runTest {
        coEvery {
            rideBookingRepository.getDriverDailyReport()
        } returns GetDriverDailyReportResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "Server error"
        )

        val result = useCase()

        assertTrue(result is GetDriverDailyReportResult.Error)
        val error = result as GetDriverDailyReportResult.Error
        assertEquals(RideError.NETWORK_ERROR, error.error)
        assertEquals("Server error", error.message)
        coVerify(exactly = 1) { rideBookingRepository.getDriverDailyReport() }
    }
}
