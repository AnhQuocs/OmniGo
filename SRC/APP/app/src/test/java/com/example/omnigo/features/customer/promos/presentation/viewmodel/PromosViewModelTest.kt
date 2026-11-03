package com.example.omnigo.features.customer.promos.presentation.viewmodel

import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.model.VoucherDiscountType
import com.example.omnigo.features.customer.promos.domain.model.VoucherUsage
import com.example.omnigo.features.customer.promos.domain.usecase.ApplyVoucherUseCase
import com.example.omnigo.features.customer.promos.domain.usecase.GetMyVoucherUsagesUseCase
import com.example.omnigo.features.customer.promos.domain.usecase.GetVouchersUseCase
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PromosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getVouchersUseCase: GetVouchersUseCase
    private lateinit var applyVoucherUseCase: ApplyVoucherUseCase
    private lateinit var getMyVoucherUsagesUseCase: GetMyVoucherUsagesUseCase
    private lateinit var viewModel: PromosViewModel

    private val sampleVouchers = listOf(
        Voucher(
            id = 1L,
            code = "RIDE20",
            name = "Giảm 20k đặt xe",
            description = "Áp dụng cuốc xe",
            discountType = VoucherDiscountType.FIXED_AMOUNT,
            discountValue = 20000.0,
            maxDiscountAmount = null,
            minOrderAmount = 50000.0,
            applicableService = "RIDE",
            usageLimit = 100,
            usedCount = 10,
            perUserLimit = 2,
            startDate = "2026-01-01",
            endDate = "2026-12-31",
            active = true
        ),
        Voucher(
            id = 2L,
            code = "FOOD30",
            name = "Giảm 30% đồ ăn",
            description = "Áp dụng giao đồ ăn",
            discountType = VoucherDiscountType.PERCENTAGE,
            discountValue = 30.0,
            maxDiscountAmount = 50000.0,
            minOrderAmount = 100000.0,
            applicableService = "FOOD",
            usageLimit = 50,
            usedCount = 5,
            perUserLimit = 1,
            startDate = "2026-01-01",
            endDate = "2026-12-31",
            active = true
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getVouchersUseCase = mockk()
        applyVoucherUseCase = mockk()
        getMyVoucherUsagesUseCase = mockk()

        coEvery { getVouchersUseCase(null) } returns Result.success(sampleVouchers)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load all vouchers and calculate counts`() = runTest {
        // Arrange & Act
        viewModel = PromosViewModel(getVouchersUseCase, applyVoucherUseCase, getMyVoucherUsagesUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(2, state.vouchers.size)
        assertEquals(2, state.allVouchersCount)
        assertEquals(1, state.rideVouchersCount)
        assertEquals(1, state.foodVouchersCount)
    }

    @Test
    fun `selectFilter should load filtered vouchers`() = runTest {
        // Arrange
        val rideOnly = listOf(sampleVouchers[0])
        coEvery { getVouchersUseCase("RIDE") } returns Result.success(rideOnly)
        viewModel = PromosViewModel(getVouchersUseCase, applyVoucherUseCase, getMyVoucherUsagesUseCase)
        advanceUntilIdle()

        // Act
        viewModel.selectFilter(1)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(1, state.selectedFilterIndex)
        assertEquals(1, state.vouchers.size)
        assertEquals("RIDE20", state.vouchers[0].code)
        coVerify { getVouchersUseCase("RIDE") }
    }

    @Test
    fun `openDetail and closeDetail should update detailVoucher state`() = runTest {
        // Arrange
        viewModel = PromosViewModel(getVouchersUseCase, applyVoucherUseCase, getMyVoucherUsagesUseCase)
        advanceUntilIdle()

        // Act - Open
        viewModel.openDetail(sampleVouchers[0])
        assertEquals("RIDE20", viewModel.uiState.value.detailVoucher?.code)

        // Act - Close
        viewModel.closeDetail()
        assertNull(viewModel.uiState.value.detailVoucher)
    }

    @Test
    fun `copyCode should update statusMessage with copied code`() = runTest {
        // Arrange
        viewModel = PromosViewModel(getVouchersUseCase, applyVoucherUseCase, getMyVoucherUsagesUseCase)
        advanceUntilIdle()

        // Act
        viewModel.copyCode("TESTCODE")

        // Assert
        assertNotNull(viewModel.uiState.value.statusMessage)
    }

    @Test
    fun `applyManualCode should invoke usecase and update appliedPreview`() = runTest {
        // Arrange
        val expectedResult = AppliedVoucherResult(
            voucherCode = "RIDE20",
            voucherName = "Giảm 20k đặt xe",
            originalAmount = 100000.0,
            discountAmount = 20000.0,
            finalAmount = 80000.0,
            message = "Success"
        )
        coEvery { applyVoucherUseCase("RIDE20", any(), any()) } returns Result.success(expectedResult)
        viewModel = PromosViewModel(getVouchersUseCase, applyVoucherUseCase, getMyVoucherUsagesUseCase)
        advanceUntilIdle()

        // Act
        viewModel.updatePromoCodeInput("RIDE20")
        var selectedCode: String? = null
        viewModel.applyManualCode(currentOrderSubtotal = 100000.0, onSelectVoucher = { selectedCode = it })
        advanceUntilIdle()

        // Assert
        assertEquals("RIDE20", selectedCode)
        assertEquals(expectedResult, viewModel.uiState.value.appliedPreview)
    }

    @Test
    fun `openUsageHistory should fetch usages and update state`() = runTest {
        // Arrange
        val mockUsages = listOf(
            VoucherUsage(
                id = 10L,
                voucherId = 1L,
                voucherCode = "RIDE20",
                userId = "user_1",
                orderId = "1001",
                serviceType = "RIDE",
                discountAmount = 20000.0,
                usedAt = "2026-10-30T10:00:00Z"
            )
        )
        coEvery { getMyVoucherUsagesUseCase() } returns Result.success(mockUsages)
        viewModel = PromosViewModel(getVouchersUseCase, applyVoucherUseCase, getMyVoucherUsagesUseCase)
        advanceUntilIdle()

        // Act
        viewModel.openUsageHistory()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(true, state.showHistorySheet)
        assertEquals(1, state.usageHistory.size)
        assertEquals("RIDE20", state.usageHistory[0].voucherCode)

        // Close history
        viewModel.closeUsageHistory()
        assertEquals(false, viewModel.uiState.value.showHistorySheet)
    }
}

