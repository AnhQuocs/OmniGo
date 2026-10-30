package com.example.omnigo.features.customer.ride.data.repository

import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.ride.data.remote.api.RideBookingApi
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.data.remote.realtime.RideStompSubscriptionSource
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.data.remote.dto.DriverDailyReportDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RideBookingDailyReportCacheTest {

    private lateinit var rideBookingApi: RideBookingApi
    private lateinit var stompSource: RideStompSubscriptionSource
    private lateinit var repository: RideBookingRepositoryImpl

    private var currentTime = 1_000_000L

    private val sampleDto = DriverDailyReportDto(
        totalTrips = 5,
        totalRevenue = 250000.0,
        driverEarnings = 200000.0,
        platformFee = 50000.0,
        cashCollected = 150000.0,
        appPaid = 100000.0
    )

    private val sampleApiResponse = ApiResponse(
        success = true,
        message = null,
        data = sampleDto,
        timestamp = null
    )

    private val sampleBookingResponse = BookingResponse(
        bookingId = 99L,
        status = "COMPLETED"
    )

    @Before
    fun setUp() {
        rideBookingApi = mockk()
        stompSource = mockk<RideStompSubscriptionSource>(relaxed = true)
        repository = RideBookingRepositoryImpl(rideBookingApi, stompSource).apply {
            clock = { currentTime }
        }
    }

    @Test
    fun `first getDriverDailyReport call fetches from API and caches result`() = runTest {
        coEvery { rideBookingApi.getDriverDailyReport(any(), any()) } returns sampleApiResponse

        val result = repository.getDriverDailyReport(forceRefresh = false)

        assertTrue(result is GetDriverDailyReportResult.Success)
        assertEquals(5, (result as GetDriverDailyReportResult.Success).report.totalTrips)
        coVerify(exactly = 1) { rideBookingApi.getDriverDailyReport(any(), any()) }
    }

    @Test
    fun `subsequent calls within TTL reuse in-memory cache without calling API`() = runTest {
        coEvery { rideBookingApi.getDriverDailyReport(any(), any()) } returns sampleApiResponse

        // First call - triggers network
        repository.getDriverDailyReport(forceRefresh = false)
        // Advance time by 2 minutes (TTL is 5 minutes)
        currentTime += 2 * 60 * 1000L
        // Second call
        val cachedResult = repository.getDriverDailyReport(forceRefresh = false)

        assertTrue(cachedResult is GetDriverDailyReportResult.Success)
        assertEquals(5, (cachedResult as GetDriverDailyReportResult.Success).report.totalTrips)
        coVerify(exactly = 1) { rideBookingApi.getDriverDailyReport(any(), any()) }
    }

    @Test
    fun `call after TTL expiration triggers new API fetch`() = runTest {
        coEvery { rideBookingApi.getDriverDailyReport(any(), any()) } returns sampleApiResponse

        repository.getDriverDailyReport(forceRefresh = false)
        // Advance time past 5-minute TTL (e.g. 6 minutes)
        currentTime += 6 * 60 * 1000L

        repository.getDriverDailyReport(forceRefresh = false)

        coVerify(exactly = 2) { rideBookingApi.getDriverDailyReport(any(), any()) }
    }

    @Test
    fun `forceRefresh bypasses valid cache and calls API immediately`() = runTest {
        coEvery { rideBookingApi.getDriverDailyReport(any(), any()) } returns sampleApiResponse

        repository.getDriverDailyReport(forceRefresh = false)
        // Only 30 seconds later, but forceRefresh = true
        currentTime += 30 * 1000L
        repository.getDriverDailyReport(forceRefresh = true)

        coVerify(exactly = 2) { rideBookingApi.getDriverDailyReport(any(), any()) }
    }

    @Test
    fun `invalidateDailyReportCache clears cache and next fetch calls API`() = runTest {
        coEvery { rideBookingApi.getDriverDailyReport(any(), any()) } returns sampleApiResponse

        repository.getDriverDailyReport(forceRefresh = false)
        repository.invalidateDailyReportCache()
        repository.getDriverDailyReport(forceRefresh = false)

        coVerify(exactly = 2) { rideBookingApi.getDriverDailyReport(any(), any()) }
    }

    @Test
    fun `completeBooking invalidates daily report cache upon success`() = runTest {
        coEvery { rideBookingApi.getDriverDailyReport(any(), any()) } returns sampleApiResponse
        coEvery { rideBookingApi.completeBooking(99L, "CASH") } returns sampleBookingResponse

        repository.getDriverDailyReport(forceRefresh = false)
        repository.completeBooking(99L, "CASH")
        repository.getDriverDailyReport(forceRefresh = false)

        coVerify(exactly = 2) { rideBookingApi.getDriverDailyReport(any(), any()) }
    }
}
