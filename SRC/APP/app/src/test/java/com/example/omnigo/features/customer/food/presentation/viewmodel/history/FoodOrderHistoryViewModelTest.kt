package com.example.omnigo.features.customer.food.presentation.viewmodel.history

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.usecase.GetMyFoodOrdersUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
class FoodOrderHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getMyFoodOrdersUseCase: GetMyFoodOrdersUseCase
    private lateinit var viewModel: FoodOrderHistoryViewModel

    private val pendingOrder = FoodOrder(
        id = 1L,
        customerId = 10L,
        driverId = null,
        restaurantId = 1L,
        restaurantName = "Phở Hà Nội",
        restaurantAddress = "123 Cầu Giấy",
        status = "PENDING",
        paymentMethod = PaymentMethod.CASH,
        isPaid = false,
        itemsPrice = 50000.0,
        deliveryFee = 15000.0,
        totalPrice = 65000.0,
        dropOffAddress = "456 Láng",
        dropOffLatitude = 21.0,
        dropOffLongitude = 105.0,
        note = "",
        items = emptyList(),
        estimatedDeliveryMinutes = 20,
        createdAt = "2026-09-29T12:00:00Z"
    )

    private val completedOrder = FoodOrder(
        id = 2L,
        customerId = 10L,
        driverId = 5L,
        restaurantId = 2L,
        restaurantName = "Bún Chả",
        restaurantAddress = "78 Phố Huế",
        status = "COMPLETED",
        paymentMethod = PaymentMethod.WALLET,
        isPaid = true,
        itemsPrice = 60000.0,
        deliveryFee = 15000.0,
        totalPrice = 75000.0,
        dropOffAddress = "456 Láng",
        dropOffLatitude = 21.0,
        dropOffLongitude = 105.0,
        note = "",
        items = emptyList(),
        estimatedDeliveryMinutes = 0,
        createdAt = "2026-09-28T12:00:00Z"
    )

    @Before
    fun setUp() {
        getMyFoodOrdersUseCase = mockk()
    }

    @Test
    fun `init loads order history successfully`() = runTest {
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(
            listOf(pendingOrder, completedOrder)
        )

        viewModel = FoodOrderHistoryViewModel(getMyFoodOrdersUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals(2, state.orders.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `filter tab changes update selectedFilter state`() = runTest {
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(
            listOf(pendingOrder, completedOrder)
        )

        viewModel = FoodOrderHistoryViewModel(getMyFoodOrdersUseCase)
        advanceUntilIdle()

        // Filter: ONGOING
        viewModel.onFilterSelected(HistoryFilter.ONGOING)
        assertEquals(HistoryFilter.ONGOING, viewModel.uiState.value.selectedFilter)

        // Filter: DELIVERED
        viewModel.onFilterSelected(HistoryFilter.DELIVERED)
        assertEquals(HistoryFilter.DELIVERED, viewModel.uiState.value.selectedFilter)

        // Filter: CANCELLED
        viewModel.onFilterSelected(HistoryFilter.CANCELLED)
        assertEquals(HistoryFilter.CANCELLED, viewModel.uiState.value.selectedFilter)

        // Filter: ALL
        viewModel.onFilterSelected(HistoryFilter.ALL)
        assertEquals(HistoryFilter.ALL, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `syncOrdersSilently updates orders list without flashing loading indicator`() = runTest {
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(
            listOf(pendingOrder)
        )

        viewModel = FoodOrderHistoryViewModel(getMyFoodOrdersUseCase)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.orders.size)

        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(
            listOf(pendingOrder, completedOrder)
        )

        viewModel.syncOrdersSilently()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(2, viewModel.uiState.value.orders.size)
        coVerify(exactly = 2) { getMyFoodOrdersUseCase() }
    }

    @Test
    fun `loadOrders failure updates errorMessage`() = runTest {
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Error(
            FoodError.NETWORK_ERROR,
            "Network error"
        )

        viewModel = FoodOrderHistoryViewModel(getMyFoodOrdersUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.orders.isEmpty())
        assertNotNull(state.errorMessage)

        viewModel.onClearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `refresh keeps existing orders and updates isRefreshing`() = runTest {
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(
            listOf(pendingOrder)
        )

        viewModel = FoodOrderHistoryViewModel(getMyFoodOrdersUseCase)
        advanceUntilIdle()

        viewModel.loadOrders(isRefresh = true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(1, viewModel.uiState.value.orders.size)
        coVerify(exactly = 2) { getMyFoodOrdersUseCase() }
    }
}


