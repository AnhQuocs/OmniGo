package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CancelFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.DriverProfile
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult
import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.RetryDriverResult
import com.example.omnigo.features.customer.food.domain.model.SwitchToCashResult
import com.example.omnigo.features.customer.food.domain.usecase.CancelFoodOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodOrderDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.RetryDriverUseCase
import com.example.omnigo.features.customer.food.domain.usecase.SwitchToCashUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodOrderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getFoodOrderDetailUseCase: GetFoodOrderDetailUseCase
    private lateinit var getDriverProfileUseCase: GetDriverProfileUseCase
    private lateinit var cancelFoodOrderUseCase: CancelFoodOrderUseCase
    private lateinit var switchToCashUseCase: SwitchToCashUseCase
    private lateinit var retryDriverUseCase: RetryDriverUseCase
    private lateinit var viewModel: FoodOrderDetailViewModel

    private val mockOrder = FoodOrder(
        id = 123L,
        customerId = 1L,
        driverId = 77L,
        driverName = "Trần Shipper",
        driverPhone = "0988888888",
        driverVehiclePlate = "30H-99999",
        restaurantId = 5L,
        restaurantName = "Bún Chả Hàng Quạt",
        restaurantAddress = "74 Hàng Quạt, Hoàn Kiếm",
        status = "PREPARING",
        paymentMethod = PaymentMethod.WALLET,
        isPaid = true,
        itemsPrice = 60000.0,
        deliveryFee = 15000.0,
        totalPrice = 75000.0,
        dropOffAddress = "69 Nguyễn Hy Quang, Đống Đa, Hà Nội",
        dropOffLatitude = 21.0150,
        dropOffLongitude = 105.8520,
        note = "Nhiều ớt",
        items = listOf(
            FoodOrderItem(
                id = 10L,
                menuItemId = 201L,
                itemName = "Bún Chả Đặc Biệt",
                itemPrice = 60000.0,
                quantity = 1,
                subtotal = 60000.0,
                note = ""
            )
        ),
        estimatedDeliveryMinutes = 20,
        createdAt = "2026-09-29T12:00:00Z"
    )

    @Before
    fun setUp() {
        getFoodOrderDetailUseCase = mockk()
        getDriverProfileUseCase = mockk()
        cancelFoodOrderUseCase = mockk()
        switchToCashUseCase = mockk()
        retryDriverUseCase = mockk()
        coEvery { getDriverProfileUseCase(any()) } returns GetDriverProfileResult.Error(
            FoodError.UNKNOWN_ERROR
        )
    }

    private fun createViewModel(orderId: Long = 123L): FoodOrderDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("orderId" to orderId))
        return FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            cancelFoodOrderUseCase = cancelFoodOrderUseCase,
            switchToCashUseCase = switchToCashUseCase,
            retryDriverUseCase = retryDriverUseCase,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun `init loads order detail successfully`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNotNull(state.order)
        assertEquals(123L, state.order?.id)
        assertEquals("Bún Chả Hàng Quạt", state.order?.restaurantName)
        assertEquals("PREPARING", state.order?.status)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadOrderDetail failure updates errorMessage`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Error(
            FoodError.SERVER_ERROR,
            "Server connection error"
        )

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.order)
        assertNotNull(state.errorMessage)

        viewModel.onClearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `cancel order success updates order and actionMessage`() = runTest {
        val cancelledOrder = mockOrder.copy(status = "CANCELLED")
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)
        coEvery { cancelFoodOrderUseCase(123L, "CHANGED_MIND", "Changed mind") } returns CancelFoodOrderResult.Success(cancelledOrder)

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        viewModel.onOpenCancelDialog()
        assertTrue(viewModel.uiState.value.isCancelDialogOpen)

        viewModel.onCancelOrder("CHANGED_MIND", "Changed mind")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCancelDialogOpen)
        assertFalse(state.isActionLoading)
        assertEquals("CANCELLED", state.order?.status)
        assertNotNull(state.actionMessage)

        viewModel.onClearActionMessage()
        assertNull(viewModel.uiState.value.actionMessage)
    }

    @Test
    fun `switch to cash success updates order paymentMethod`() = runTest {
        val cashOrder = mockOrder.copy(paymentMethod = PaymentMethod.CASH)
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)
        coEvery { switchToCashUseCase(123L) } returns SwitchToCashResult.Success(cashOrder)

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        viewModel.onSwitchToCash()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isActionLoading)
        assertEquals(PaymentMethod.CASH, state.order?.paymentMethod)
        assertNotNull(state.actionMessage)
    }

    @Test
    fun `retry driver increments retryDriverCount up to limit 3`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)
        coEvery { retryDriverUseCase(123L) } returns RetryDriverResult.Success(mockOrder)

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.retryDriverCount)

        viewModel.onRetryDriver()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.retryDriverCount)

        viewModel.onRetryDriver()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.retryDriverCount)

        viewModel.onRetryDriver()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.retryDriverCount)

        // 4th time -> should not call useCase, shows limit message
        viewModel.onRetryDriver()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.retryDriverCount)
    }

    @Test
    fun `onRetry reloads order detail`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns
            GetFoodOrderDetailResult.Error(FoodError.NETWORK_ERROR) andThen
            GetFoodOrderDetailResult.Success(mockOrder)

        viewModel = createViewModel(123L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.onRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(123L, state.order?.id)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadOrderDetail with isRefresh updates isRefreshing state`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        viewModel.loadOrderDetail(123L, isRefresh = true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(123L, viewModel.uiState.value.order?.id)
    }

    @Test
    fun `refresh failure keeps previously loaded order visible`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns
            GetFoodOrderDetailResult.Success(mockOrder) andThen
            GetFoodOrderDetailResult.Error(FoodError.NETWORK_ERROR)

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        viewModel.loadOrderDetail(isRefresh = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(mockOrder, state.order)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `loads driver profile using the driver id from order detail`() = runTest {
        val profile = DriverProfile(
            driverId = 77L,
            driverName = "Driver Name",
            driverPhone = "0900000000",
            vehiclePlate = "ABC-123",
            avatarUrl = null
        )
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)
        coEvery { getDriverProfileUseCase(77L) } returns GetDriverProfileResult.Success(profile)

        viewModel = createViewModel(123L)
        advanceUntilIdle()

        assertEquals(profile, viewModel.uiState.value.driverProfile)
        assertFalse(viewModel.uiState.value.isDriverProfileLoading)
    }
}
