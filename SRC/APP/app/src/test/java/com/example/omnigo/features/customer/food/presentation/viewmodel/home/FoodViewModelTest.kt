package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantsUseCase
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
class FoodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getRestaurantsUseCase: GetRestaurantsUseCase
    private lateinit var cartManager: CartManager
    private lateinit var viewModel: FoodViewModel

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
        menuItems = emptyList()
    )

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

    @Before
    fun setUp() {
        getRestaurantsUseCase = mockk()
        cartManager = CartManager()
    }

    @Test
    fun `init loads restaurants successfully`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(listOf(mockRestaurant))

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.restaurants.size)
        assertEquals(mockRestaurant, state.restaurants.first())
        assertNull(state.errorMessage)
    }

    @Test
    fun `init loads restaurants with error sets errorMessage`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Error(FoodError.NETWORK_ERROR)

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.restaurants.isEmpty())
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `onCategorySelected toggles category selection`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(emptyList())

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager)

        viewModel.onCategorySelected("Phở")
        assertEquals("Phở", viewModel.uiState.value.selectedCategory)

        // Toggle same category removes selection
        viewModel.onCategorySelected("Phở")
        assertNull(viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `cart state updates are reflected in FoodUiState`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(emptyList())

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager)

        assertEquals(0, viewModel.uiState.value.totalCartQuantity)
        assertEquals(0.0, viewModel.uiState.value.totalCartAmount, 0.01)
        assertNull(viewModel.uiState.value.cartRestaurantId)

        cartManager.addItem(mockMenuItem, quantity = 2, restaurantName = "Phở Thìn")

        val state = viewModel.uiState.value
        assertEquals(2, state.totalCartQuantity)
        assertEquals(100000.0, state.totalCartAmount, 0.01)
        assertEquals(1L, state.cartRestaurantId)
    }
}
