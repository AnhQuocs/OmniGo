package com.example.omnigo.features.driver.earnings.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class DriverEarningsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getDriverDailyReportUseCase: GetDriverDailyReportUseCase

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
        Dispatchers.setMain(testDispatcher)
        getDriverDailyReportUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads earnings report successfully`() = runTest {
        coEvery { getDriverDailyReportUseCase(false) } returns GetDriverDailyReportResult.Success(sampleReport)

        val viewModel = DriverEarningsViewModel(getDriverDailyReportUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(EarningsPeriod.TODAY, state.selectedPeriod)
        assertNotNull(state.dailyReport)
        assertEquals(8, state.dailyReport?.totalTrips)
        assertEquals(320000.0, state.dailyReport?.driverEarnings ?: 0.0, 0.01)
        coVerify(exactly = 1) { getDriverDailyReportUseCase(false) }
    }

    @Test
    fun `onPeriodSelected updates selectedPeriod and triggers forceRefresh`() = runTest {
        coEvery { getDriverDailyReportUseCase(any()) } returns GetDriverDailyReportResult.Success(sampleReport)

        val viewModel = DriverEarningsViewModel(getDriverDailyReportUseCase)
        advanceUntilIdle()

        viewModel.onPeriodSelected(EarningsPeriod.THIS_WEEK)
        advanceUntilIdle()

        assertEquals(EarningsPeriod.THIS_WEEK, viewModel.uiState.value.selectedPeriod)
        coVerify(exactly = 1) { getDriverDailyReportUseCase(true) }

        // When selecting same period, no extra call
        viewModel.onPeriodSelected(EarningsPeriod.THIS_WEEK)
        advanceUntilIdle()
        coVerify(exactly = 1) { getDriverDailyReportUseCase(true) }
    }

    @Test
    fun `setShowCashoutDialog and setShowTopupDialog update states correctly`() = runTest {
        coEvery { getDriverDailyReportUseCase(false) } returns GetDriverDailyReportResult.Success(sampleReport)

        val viewModel = DriverEarningsViewModel(getDriverDailyReportUseCase)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCashoutDialog)
        assertFalse(viewModel.uiState.value.showTopupDialog)

        viewModel.setShowCashoutDialog(true)
        assertTrue(viewModel.uiState.value.showCashoutDialog)

        viewModel.setShowCashoutDialog(false)
        assertFalse(viewModel.uiState.value.showCashoutDialog)

        viewModel.setShowTopupDialog(true)
        assertTrue(viewModel.uiState.value.showTopupDialog)

        viewModel.setShowTopupDialog(false)
        assertFalse(viewModel.uiState.value.showTopupDialog)
    }
}

