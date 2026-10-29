package com.example.omnigo.features.customer.food.presentation.viewmodel.history

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetMyFoodOrdersUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantItemsUseCase
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
    private lateinit var getRestaurantDetailUseCase: GetRestaurantDetailUseCase
    private lateinit var getRestaurantItemsUseCase: GetRestaurantItemsUseCase
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
        getRestaurantDetailUseCase = mockk(relaxed = true)
        getRestaurantItemsUseCase = mockk(relaxed = true)
    }

    private fun createViewModel() = FoodOrderHistoryViewModel(
        getMyFoodOrdersUseCase = getMyFoodOrdersUseCase,
        getRestaurantDetailUseCase = getRestaurantDetailUseCase,
        getRestaurantItemsUseCase = getRestaurantItemsUseCase
    )

    @Test
    fun `init loads order history successfully`() = runTest {
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(
            listOf(pendingOrder, completedOrder)
        )

        viewModel = createViewModel()
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

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFilterSelected(HistoryFilter.ONGOING)
        assertEquals(HistoryFilter.ONGOING, viewModel.uiState.value.selectedFilter)

        viewModel.onFilterSelected(HistoryFilter.DELIVERED)
        assertEquals(HistoryFilter.DELIVERED, viewModel.uiState.value.selectedFilter)

        viewModel.onFilterSelected(HistoryFilter.CANCELLED)
        assertEquals(HistoryFilter.CANCELLED, viewModel.uiState.value.selectedFilter)

        viewModel.onFilterSelected(HistoryFilter.ALL)
        assertEquals(HistoryFilter.ALL, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `syncOrdersSilently updates orders list without flashing loading indicator`() = runTest {
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(
            listOf(pendingOrder)
        )

        viewModel = createViewModel()
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

        viewModel = createViewModel()
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

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadOrders(isRefresh = true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(1, viewModel.uiState.value.orders.size)
        coVerify(exactly = 2) { getMyFoodOrdersUseCase() }
    }

    @Test
    fun `loadOrders enriches restaurant image, address, and item thumbnails when missing`() = runTest {
        val unhydratedOrder = pendingOrder.copy(
            restaurantImageUrl = null,
            restaurantAddress = null,
            items = listOf(
                FoodOrderItem(
                    id = 10L,
                    menuItemId = 100L,
                    itemName = "Phở Bò",
                    itemPrice = 50000.0,
                    quantity = 2,
                    subtotal = 100000.0,
                    note = "",
                    imageUrl = null
                )
            )
        )

        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(listOf(unhydratedOrder))
        coEvery { getRestaurantDetailUseCase(1L) } returns GetRestaurantDetailResult.Success(
            Restaurant(
                id = 1L,
                name = "Phở Hà Nội",
                phone = "0901234567",
                address = "13 Lò Đúc, Hai Bà Trưng, Hà Nội",
                latitude = 21.01,
                longitude = 105.85,
                imageUrl = "https://example.com/restaurant.jpg",
                status = "ACTIVE",
                openTime = "06:00",
                closeTime = "22:00",
                rating = 4.9,
                reviewCount = 120,
                isOpen = true,
                menuItems = listOf(
                    MenuItem(
                        id = 100L,
                        restaurantId = 1L,
                        name = "Phở Bò",
                        description = "",
                        price = 50000.0,
                        imageUrl = "https://example.com/pho_bo.jpg",
                        category = "Phở",
                        isAvailable = true
                    )
                )
            )
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val enrichedOrder = viewModel.uiState.value.orders.first()
        assertEquals("https://example.com/restaurant.jpg", enrichedOrder.restaurantImageUrl)
        assertEquals("13 Lò Đúc, Hai Bà Trưng, Hà Nội", enrichedOrder.restaurantAddress)
        assertEquals("https://example.com/pho_bo.jpg", enrichedOrder.items.first().imageUrl)
    }
}
