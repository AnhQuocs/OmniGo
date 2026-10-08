package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.CreateFoodOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
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
class FoodCheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var cartManager: CartManager
    private lateinit var userLocationManager: UserLocationManager
    private lateinit var createFoodOrderUseCase: CreateFoodOrderUseCase
    private lateinit var getRestaurantDetailUseCase: GetRestaurantDetailUseCase
    private lateinit var viewModel: FoodCheckoutViewModel

    private val mockMenuItem = MenuItem(
        id = 101L,
        restaurantId = 1L,
        name = "Phở Tái",
        description = "Bò tái mềm",
        price = 50000.0,
        imageUrl = "https://example.com/pho.jpg",
        category = "Phở",
        isAvailable = true
    )

    private val mockRestaurant = Restaurant(
        id = 1L,
        name = "Phở Thìn Lò Đúc",
        phone = "0987654321",
        address = "13 Lò Đúc, Hai Bà Trưng, Hà Nội",
        latitude = 21.0180,
        longitude = 105.8560,
        imageUrl = "https://example.com/pho.jpg",
        status = "OPEN",
        openTime = "06:00",
        closeTime = "21:00",
        rating = 4.9,
        reviewCount = 350,
        isOpen = true,
        menuItems = listOf(mockMenuItem)
    )

    private val mockOrder = FoodOrder(
        id = 888L,
        customerId = 1L,
        driverId = null,
        restaurantId = 1L,
        restaurantName = "Phở Thìn Lò Đúc",
        status = "PENDING",
        paymentMethod = PaymentMethod.CASH,
        isPaid = false,
        itemsPrice = 100000.0,
        deliveryFee = 15000.0,
        totalPrice = 115000.0,
        dropOffAddress = "69 Nguyễn Hy Quang, Đống Đa, Hà Nội",
        dropOffLatitude = 21.0150,
        dropOffLongitude = 105.8520,
        note = "Ít ớt",
        items = emptyList(),
        estimatedDeliveryMinutes = 20,
        createdAt = "2026-09-29T12:00:00Z"
    )

    @Before
    fun setUp() {
        cartManager = CartManager()
        userLocationManager = UserLocationManager()
        createFoodOrderUseCase = mockk()
        getRestaurantDetailUseCase = mockk()

        coEvery { getRestaurantDetailUseCase(1L) } returns GetRestaurantDetailResult.Success(mockRestaurant)

        viewModel = FoodCheckoutViewModel(
            cartManager = cartManager,
            userLocationManager = userLocationManager,
            createFoodOrderUseCase = createFoodOrderUseCase,
            getRestaurantDetailUseCase = getRestaurantDetailUseCase
        )
    }

    @Test
    fun `initial state observes location and cart correctly`() = runTest {
        userLocationManager.updateLocation(UserLocation(21.0150, 105.8520, "Nhà", "69 Nguyễn Hy Quang, Đống Đa, Hà Nội"))
        cartManager.addItem(mockMenuItem, quantity = 2, restaurantName = "Phở Thìn Lò Đúc")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1L, state.restaurantId)
        assertEquals("Phở Thìn Lò Đúc", state.restaurantName)
        assertEquals(1, state.items.size)
        assertEquals(100000.0, state.itemsSubtotal, 0.01)
        assertEquals("69 Nguyễn Hy Quang, Đống Đa, Hà Nội", state.deliveryAddress)
        assertTrue(state.totalAmount > 100000.0)
    }

    @Test
    fun `onPaymentMethodSelected updates payment method`() {
        viewModel.onPaymentMethodSelected(PaymentMethod.WALLET)
        assertEquals(PaymentMethod.WALLET, viewModel.uiState.value.selectedPaymentMethod)

        viewModel.onPaymentMethodSelected(PaymentMethod.MOMO)
        assertEquals(PaymentMethod.MOMO, viewModel.uiState.value.selectedPaymentMethod)
    }

    @Test
    fun `onDriverNoteChanged updates note`() {
        viewModel.onDriverNoteChanged("Gọi trước 5 phút")
        assertEquals("Gọi trước 5 phút", viewModel.uiState.value.driverNote)
    }

    @Test
    fun `onDeliveryAddressChanged updates address and recalculates total`() {
        viewModel.onDeliveryAddressChanged("12 Liễu Giai, Ba Đình", 21.0330, 105.8150)
        assertEquals("12 Liễu Giai, Ba Đình", viewModel.uiState.value.deliveryAddress)
        assertEquals(21.0330, viewModel.uiState.value.deliveryLatitude, 0.0001)
    }

    @Test
    fun `onPlaceOrder with empty cart sets errorMessage`() {
        viewModel.onPlaceOrder()
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onPlaceOrder success places order, clears cart and updates createdOrderId`() = runTest {
        userLocationManager.updateLocation(UserLocation(21.0150, 105.8520, "Nhà", "69 Nguyễn Hy Quang, Đống Đa, Hà Nội"))
        cartManager.addItem(mockMenuItem, quantity = 2, restaurantName = "Phở Thìn Lò Đúc")
        advanceUntilIdle()

        coEvery { createFoodOrderUseCase(any(), any(), any(), any(), any(), any(), any()) } returns CreateFoodOrderResult.Success(mockOrder)

        viewModel.onPlaceOrder()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isPlacingOrder)
        assertEquals(888L, state.createdOrderId)
        assertNull(state.errorMessage)

        // Cart should be cleared after successful order placement
        assertTrue(cartManager.cartState.value.isEmpty)
    }

    @Test
    fun `onPlaceOrder failure sets errorMessage and does not clear cart`() = runTest {
        userLocationManager.updateLocation(UserLocation(21.0150, 105.8520, "Nhà", "69 Nguyễn Hy Quang, Đống Đa, Hà Nội"))
        cartManager.addItem(mockMenuItem, quantity = 1, restaurantName = "Phở Thìn Lò Đúc")
        advanceUntilIdle()

        coEvery { createFoodOrderUseCase(any(), any(), any(), any(), any(), any(), any()) } returns CreateFoodOrderResult.Error(FoodError.SERVER_ERROR, "Out of stock")

        viewModel.onPlaceOrder()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isPlacingOrder)
        assertNull(state.createdOrderId)
        assertNotNull(state.errorMessage)

        // Cart MUST NOT be cleared on failure
        assertFalse(cartManager.cartState.value.isEmpty)
        assertEquals(1, cartManager.cartState.value.items.size)

        viewModel.onClearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onPlaceOrder when already placing order ignores subsequent calls`() = runTest {
        userLocationManager.updateLocation(UserLocation(21.0150, 105.8520, "Nhà", "69 Nguyễn Hy Quang, Đống Đa, Hà Nội"))
        cartManager.addItem(mockMenuItem, quantity = 1, restaurantName = "Phở Thìn Lò Đúc")
        advanceUntilIdle()

        coEvery { createFoodOrderUseCase(any(), any(), any(), any(), any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            CreateFoodOrderResult.Success(mockOrder)
        }

        viewModel.onPlaceOrder()
        assertTrue(viewModel.uiState.value.isPlacingOrder)

        // Second call while placing order should be ignored
        viewModel.onPlaceOrder()
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 1) {
            createFoodOrderUseCase(any(), any(), any(), any(), any(), any(), any())
        }
    }
}
