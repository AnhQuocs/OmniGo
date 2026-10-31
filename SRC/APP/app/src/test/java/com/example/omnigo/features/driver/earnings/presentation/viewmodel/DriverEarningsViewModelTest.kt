package com.example.omnigo.features.driver.earnings.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.customer.ride.domain.model.GetDriverBookingsResult
import com.example.omnigo.features.driver.domain.usecase.GetDriverBookingsUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.CreateDriverDepositUrlUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.DriverWithdrawUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverTransactionsUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverWalletUseCase
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
    private val driverRepository: DriverRepository = mockk(relaxed = true)
    private val driverWithdrawUseCase: DriverWithdrawUseCase = mockk(relaxed = true)
    private val createDriverDepositUrlUseCase: CreateDriverDepositUrlUseCase = mockk(relaxed = true)
    private val getDriverWalletUseCase: GetDriverWalletUseCase = mockk(relaxed = true)
    private val getDriverTransactionsUseCase: GetDriverTransactionsUseCase = mockk(relaxed = true)
    private val getDriverBookingsUseCase: GetDriverBookingsUseCase = mockk(relaxed = true)

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
        coEvery { getDriverWalletUseCase() } returns Result.success(mockk(relaxed = true))
        coEvery { getDriverTransactionsUseCase() } returns Result.success(emptyList())
        coEvery { getDriverBookingsUseCase() } returns GetDriverBookingsResult.Success(emptyList())
        coEvery { driverRepository.getDriverAllFoodOrders() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads earnings report successfully`() = runTest {
        coEvery {
            getDriverDailyReportUseCase(forceRefresh = false, period = "DAY", date = null)
        } returns GetDriverDailyReportResult.Success(sampleReport)

        val viewModel = DriverEarningsViewModel(getDriverDailyReportUseCase, driverRepository, driverWithdrawUseCase, createDriverDepositUrlUseCase, getDriverWalletUseCase, getDriverTransactionsUseCase, getDriverBookingsUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(EarningsPeriod.TODAY, state.selectedPeriod)
        assertNotNull(state.dailyReport)
        assertEquals(8, state.dailyReport?.totalTrips)
        assertEquals(320000.0, state.dailyReport?.driverEarnings ?: 0.0, 0.01)
        coVerify(exactly = 1) {
            getDriverDailyReportUseCase(forceRefresh = false, period = "DAY", date = null)
        }
    }

    @Test
    fun `onPeriodSelected updates selectedPeriod and triggers forceRefresh with api period`() = runTest {
        coEvery {
            getDriverDailyReportUseCase(forceRefresh = any(), period = any(), date = any())
        } returns GetDriverDailyReportResult.Success(sampleReport)

        val viewModel = DriverEarningsViewModel(getDriverDailyReportUseCase, driverRepository, driverWithdrawUseCase, createDriverDepositUrlUseCase, getDriverWalletUseCase, getDriverTransactionsUseCase, getDriverBookingsUseCase)
        advanceUntilIdle()

        viewModel.onPeriodSelected(EarningsPeriod.THIS_WEEK)
        advanceUntilIdle()

        assertEquals(EarningsPeriod.THIS_WEEK, viewModel.uiState.value.selectedPeriod)
        coVerify(exactly = 1) {
            getDriverDailyReportUseCase(forceRefresh = true, period = "WEEK", date = null)
        }

        // When selecting same period, no extra call
        viewModel.onPeriodSelected(EarningsPeriod.THIS_WEEK)
        advanceUntilIdle()
        coVerify(exactly = 1) {
            getDriverDailyReportUseCase(forceRefresh = true, period = "WEEK", date = null)
        }
    }

    @Test
    fun `setShowCashoutSheet and setShowTopupSheet update states correctly`() = runTest {
        coEvery {
            getDriverDailyReportUseCase(forceRefresh = any(), period = any(), date = any())
        } returns GetDriverDailyReportResult.Success(sampleReport)

        val viewModel = DriverEarningsViewModel(getDriverDailyReportUseCase, driverRepository, driverWithdrawUseCase, createDriverDepositUrlUseCase, getDriverWalletUseCase, getDriverTransactionsUseCase, getDriverBookingsUseCase)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCashoutSheet)
        assertFalse(viewModel.uiState.value.showTopupSheet)

        viewModel.setShowCashoutSheet(true)
        assertTrue(viewModel.uiState.value.showCashoutSheet)

        viewModel.setShowCashoutSheet(false)
        assertFalse(viewModel.uiState.value.showCashoutSheet)

        viewModel.setShowTopupSheet(true)
        assertTrue(viewModel.uiState.value.showTopupSheet)

        viewModel.setShowTopupSheet(false)
        assertFalse(viewModel.uiState.value.showTopupSheet)
    }
}
