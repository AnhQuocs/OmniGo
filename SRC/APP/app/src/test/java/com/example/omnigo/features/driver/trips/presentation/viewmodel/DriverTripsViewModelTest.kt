package com.example.omnigo.features.driver.trips.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverTripsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getDriverDailyReportUseCase: GetDriverDailyReportUseCase
    private lateinit var driverRepository: DriverRepository
    private lateinit var driverTripSessionManager: DriverTripSessionManager

    private val activeBookingFlow = MutableStateFlow<Booking?>(null)

    private val sampleReport = DriverDailyReport(
        totalTrips = 6,
        totalRevenue = 300000.0,
        driverEarnings = 240000.0,
        platformFee = 60000.0,
        cashCollected = 200000.0,
        appPaid = 100000.0
    )

    private val sampleBooking = Booking(
        bookingId = 999L,
        customerId = 12L,
        driverId = 33L,
        status = BookingStatus.IN_PROGRESS,
        startLatitude = 21.0,
        startLongitude = 105.0,
        endLatitude = 21.1,
        endLongitude = 105.1
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getDriverDailyReportUseCase = mockk()
        driverRepository = mockk()
        driverTripSessionManager = mockk()
        every { driverTripSessionManager.activeBooking } returns activeBookingFlow
        coEvery { driverRepository.getDriverAllFoodOrders() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads daily report and reflects active booking from session manager`() = runTest {
        coEvery {
            getDriverDailyReportUseCase(false)
        } returns GetDriverDailyReportResult.Success(sampleReport)

        val viewModel = DriverTripsViewModel(
            getDriverDailyReportUseCase,
            driverRepository,
            driverTripSessionManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isReportLoading)
        assertNotNull(state.dailyReport)
        assertEquals(6, state.dailyReport?.totalTrips)
        assertEquals(240000.0, state.dailyReport?.driverEarnings ?: 0.0, 0.01)
        assertNull(state.activeBooking)

        // When active booking emits
        activeBookingFlow.value = sampleBooking
        advanceUntilIdle()

        assertEquals(999L, viewModel.uiState.value.activeBooking?.bookingId)
        coVerify(atLeast = 1) { getDriverDailyReportUseCase(false) }
        coVerify(atLeast = 1) { driverRepository.getDriverAllFoodOrders() }
    }

    @Test
    fun `loadDailyReport handles error result and updates state with error message`() = runTest {
        coEvery {
            getDriverDailyReportUseCase(false)
        } returns GetDriverDailyReportResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "Connection timeout"
        )

        val viewModel = DriverTripsViewModel(
            getDriverDailyReportUseCase,
            driverRepository,
            driverTripSessionManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isReportLoading)
        assertNull(state.dailyReport)
        assertEquals("Connection timeout", state.reportErrorMessage)
    }
}
