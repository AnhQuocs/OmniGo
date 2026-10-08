package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.DriverProfile
import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult
import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodOrderDetailUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
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
        coEvery { getDriverProfileUseCase(any()) } returns GetDriverProfileResult.Error(
            FoodError.UNKNOWN_ERROR
        )
    }

    @Test
    fun `init loads order detail successfully`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)

        val savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNotNull(state.order)
        assertEquals(123L, state.order?.id)
        assertEquals("Bún Chả Hàng Quạt", state.order?.restaurantName)
        assertEquals("PREPARING", state.order?.status)
        assertEquals(PaymentMethod.WALLET, state.order?.paymentMethod)
        assertTrue(state.order?.isPaid == true)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadOrderDetail failure updates errorMessage`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Error(
            FoodError.SERVER_ERROR,
            "Server connection error"
        )

        val savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.order)
        assertNotNull(state.errorMessage)

        viewModel.onClearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onRetry reloads order detail`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Error(FoodError.NETWORK_ERROR) andThen GetFoodOrderDetailResult.Success(mockOrder)

        val savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = savedStateHandle
        )

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

        val savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        viewModel.loadOrderDetail(123L, isRefresh = true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(123L, viewModel.uiState.value.order?.id)
    }

    @Test
    fun `init exposes loading state until order request completes`() = runTest {
        val pendingResult = CompletableDeferred<GetFoodOrderDetailResult>()
        coEvery { getFoodOrderDetailUseCase(123L) } coAnswers { pendingResult.await() }

        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        )

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.order)

        pendingResult.complete(GetFoodOrderDetailResult.Success(mockOrder))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(123L, viewModel.uiState.value.order?.id)
    }

    @Test
    fun `invalid route order id stops loading and does not call use case`() = runTest {
        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 0L))
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.canRetry)
        assertNotNull(state.errorMessage)
        io.mockk.coVerify(exactly = 0) { getFoodOrderDetailUseCase(any()) }
    }

    @Test
    fun `refresh failure keeps previously loaded order visible`() = runTest {
        coEvery { getFoodOrderDetailUseCase(123L) } returns
            GetFoodOrderDetailResult.Success(mockOrder) andThen
            GetFoodOrderDetailResult.Error(FoodError.NETWORK_ERROR)

        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        )
        advanceUntilIdle()

        viewModel.loadOrderDetail(isRefresh = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(mockOrder, state.order)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `refresh after initial error shows loading until retry completes`() = runTest {
        val pendingResult = CompletableDeferred<GetFoodOrderDetailResult>()
        var callCount = 0
        coEvery { getFoodOrderDetailUseCase(123L) } coAnswers {
            if (callCount++ == 0) {
                GetFoodOrderDetailResult.Error(FoodError.NETWORK_ERROR)
            } else {
                pendingResult.await()
            }
        }

        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.loadOrderDetail(isRefresh = true)
        assertTrue(viewModel.uiState.value.isLoading)

        pendingResult.complete(GetFoodOrderDetailResult.Success(mockOrder))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(mockOrder, viewModel.uiState.value.order)
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
        coEvery { getFoodOrderDetailUseCase(123L) } returns
            GetFoodOrderDetailResult.Success(mockOrder)
        coEvery { getDriverProfileUseCase(77L) } returns
            GetDriverProfileResult.Success(profile)

        viewModel = FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 123L))
        )
        advanceUntilIdle()

        assertEquals(profile, viewModel.uiState.value.driverProfile)
        assertFalse(viewModel.uiState.value.isDriverProfileLoading)
        io.mockk.coVerify(exactly = 1) { getDriverProfileUseCase(77L) }
    }
}
