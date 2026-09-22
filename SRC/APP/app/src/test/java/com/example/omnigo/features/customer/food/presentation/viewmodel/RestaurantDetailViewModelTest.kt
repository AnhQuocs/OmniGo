package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RestaurantDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getRestaurantDetailUseCase: GetRestaurantDetailUseCase
    private lateinit var userLocationManager: UserLocationManager
    private lateinit var viewModel: RestaurantDetailViewModel

    private val mockMenuItem1 = MenuItem(
        id = 101L,
        restaurantId = 1L,
        name = "Phở Tái",
        description = "Thịt bò tươi mềm",
        price = 50000.0,
        imageUrl = "https://example.com/pho_tai.jpg",
        category = "Phở",
        isAvailable = true
    )

    private val mockMenuItem2 = MenuItem(
        id = 102L,
        restaurantId = 1L,
        name = "Trà Đá",
        description = "Mát lạnh",
        price = 5000.0,
        imageUrl = "https://example.com/tra_da.jpg",
        category = "Đồ uống",
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
        menuItems = listOf(mockMenuItem1, mockMenuItem2)
    )

    @Before
    fun setUp() {
        getRestaurantDetailUseCase = mockk()
        userLocationManager = UserLocationManager()
        userLocationManager.updateLocation(UserLocation(21.0100, 105.8500, "Nhà", "Hà Nội"))
        viewModel = RestaurantDetailViewModel(getRestaurantDetailUseCase, userLocationManager)
    }

    @Test
    fun `loadRestaurant success updates restaurant, categories and distance`() = runTest {
        coEvery { getRestaurantDetailUseCase(1L) } returns GetRestaurantDetailResult.Success(mockRestaurant)

        viewModel.loadRestaurant(1L)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(mockRestaurant, state.restaurant)
        assertEquals(listOf("Phở", "Đồ uống"), state.categories)
        assertNotNull(state.distanceKm)
        assertNotNull(state.estimatedDeliveryMinutes)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadRestaurant error sets errorMessage`() = runTest {
        coEvery { getRestaurantDetailUseCase(999L) } returns GetRestaurantDetailResult.Error(FoodError.NETWORK_ERROR)

        viewModel.loadRestaurant(999L)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.restaurant)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `onCategorySelected updates selectedCategory`() {
        viewModel.onCategorySelected("Phở")
        assertEquals("Phở", viewModel.uiState.value.selectedCategory)

        viewModel.onCategorySelected(null)
        assertNull(viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `cart operations add, update quantity and clear correctly`() {
        // 1. Add item 1
        viewModel.onAddToCart(mockMenuItem1)
        var state = viewModel.uiState.value
        assertEquals(1, state.totalCartQuantity)
        assertEquals(50000.0, state.totalCartAmount, 0.01)
        assertEquals(1, state.cartItems[101L]?.quantity)

        // 2. Add item 1 again
        viewModel.onAddToCart(mockMenuItem1)
        state = viewModel.uiState.value
        assertEquals(2, state.totalCartQuantity)
        assertEquals(100000.0, state.totalCartAmount, 0.01)
        assertEquals(2, state.cartItems[101L]?.quantity)

        // 3. Add item 2
        viewModel.onAddToCart(mockMenuItem2)
        state = viewModel.uiState.value
        assertEquals(3, state.totalCartQuantity)
        assertEquals(105000.0, state.totalCartAmount, 0.01)

        // 4. Remove item 1
        viewModel.onRemoveFromCart(mockMenuItem1)
        state = viewModel.uiState.value
        assertEquals(2, state.totalCartQuantity)
        assertEquals(55000.0, state.totalCartAmount, 0.01)
        assertEquals(1, state.cartItems[101L]?.quantity)

        // 5. Remove item 1 again (should remove from map)
        viewModel.onRemoveFromCart(mockMenuItem1)
        state = viewModel.uiState.value
        assertEquals(1, state.totalCartQuantity)
        assertEquals(5000.0, state.totalCartAmount, 0.01)
        assertNull(state.cartItems[101L])

        // 6. Clear cart
        viewModel.onClearCart()
        state = viewModel.uiState.value
        assertEquals(0, state.totalCartQuantity)
        assertEquals(0.0, state.totalCartAmount, 0.01)
        assertTrue(state.cartItems.isEmpty())
    }
}
