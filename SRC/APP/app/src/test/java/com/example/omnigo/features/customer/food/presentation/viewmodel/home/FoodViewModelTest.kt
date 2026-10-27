package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantsUseCase
import com.example.omnigo.features.customer.food.domain.util.RestaurantSort
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
    private lateinit var userLocationManager: UserLocationManager
    private lateinit var viewModel: FoodViewModel

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

    @Before
    fun setUp() {
        getRestaurantsUseCase = mockk()
        cartManager = CartManager()
        userLocationManager = UserLocationManager()
    }

    @Test
    fun `init loads restaurants successfully and computes categories and featured`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(listOf(mockRestaurant))

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager, userLocationManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.restaurants.size)
        assertEquals(1, state.displayedRestaurants.size)
        assertEquals(listOf("Phở"), state.categories)
        assertEquals(1, state.featuredRestaurants.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `init loads restaurants with error sets errorMessage`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Error(FoodError.NETWORK_ERROR)

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager, userLocationManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.restaurants.isEmpty())
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `onCategorySelected toggles category selection and filters displayed list`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(listOf(mockRestaurant))

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager, userLocationManager)
        advanceUntilIdle()

        viewModel.onCategorySelected("Phở")
        assertEquals("Phở", viewModel.uiState.value.selectedCategory)
        assertEquals(1, viewModel.uiState.value.displayedRestaurants.size)

        // Select unmatched category
        viewModel.onCategorySelected("Pizza")
        assertEquals("Pizza", viewModel.uiState.value.selectedCategory)
        assertEquals(0, viewModel.uiState.value.displayedRestaurants.size)

        // Toggle same category removes selection
        viewModel.onCategorySelected("Pizza")
        assertNull(viewModel.uiState.value.selectedCategory)
        assertEquals(1, viewModel.uiState.value.displayedRestaurants.size)
    }

    @Test
    fun `onSortSelected updates sorting order`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(listOf(mockRestaurant))

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager, userLocationManager)
        advanceUntilIdle()

        viewModel.onSortSelected(RestaurantSort.TOP_RATED)
        assertEquals(RestaurantSort.TOP_RATED, viewModel.uiState.value.selectedSort)
    }

    @Test
    fun `onClearFilters resets search and category and reloads`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(listOf(mockRestaurant))
        coEvery { getRestaurantsUseCase("query") } returns GetRestaurantsResult.Success(emptyList())

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager, userLocationManager)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("query")
        viewModel.onCategorySelected("Phở")

        viewModel.onClearFilters()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCategory)
    }

    @Test
    fun `cart state updates are reflected in FoodUiState`() = runTest {
        coEvery { getRestaurantsUseCase(null) } returns GetRestaurantsResult.Success(emptyList())

        viewModel = FoodViewModel(getRestaurantsUseCase, cartManager, userLocationManager)

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
