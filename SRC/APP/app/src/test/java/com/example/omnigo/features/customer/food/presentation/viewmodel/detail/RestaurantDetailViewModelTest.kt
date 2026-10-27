package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantItemsUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantReviewsUseCase
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
    private lateinit var getRestaurantItemsUseCase: GetRestaurantItemsUseCase
    private lateinit var getRestaurantReviewsUseCase: GetRestaurantReviewsUseCase
    private lateinit var userLocationManager: UserLocationManager
    private lateinit var cartManager: CartManager
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

    private val mockMenuItemOtherRestaurant = MenuItem(
        id = 201L,
        restaurantId = 2L,
        name = "Bún Chả",
        description = "Chả nướng",
        price = 60000.0,
        imageUrl = "https://example.com/buncha.jpg",
        category = "Bún",
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
        getRestaurantItemsUseCase = mockk()
        getRestaurantReviewsUseCase = mockk()
        coEvery { getRestaurantReviewsUseCase(any(), any(), any(), any()) } returns Result.success(emptyList())
        userLocationManager = UserLocationManager()
        userLocationManager.updateLocation(UserLocation(21.0100, 105.8500, "Nhà", "Hà Nội"))
        cartManager = CartManager()
        viewModel = RestaurantDetailViewModel(
            getRestaurantDetailUseCase = getRestaurantDetailUseCase,
            getRestaurantItemsUseCase = getRestaurantItemsUseCase,
            getRestaurantReviewsUseCase = getRestaurantReviewsUseCase,
            userLocationManager = userLocationManager,
            cartManager = cartManager
        )
    }

    @Test
    fun `loadRestaurant success updates restaurant, categories, sections and distance`() = runTest {
        coEvery { getRestaurantDetailUseCase(1L) } returns GetRestaurantDetailResult.Success(mockRestaurant)
        coEvery { getRestaurantItemsUseCase(1L, false) } returns GetMenuItemsResult.Success(listOf(mockMenuItem1, mockMenuItem2))

        viewModel.loadRestaurant(1L)

        val state = viewModel.uiState.value
        assertFalse(state.isRestaurantLoading)
        assertFalse(state.isMenuLoading)
        assertEquals(mockRestaurant, state.restaurant)
        assertEquals(listOf("Phở", "Đồ uống"), state.categories)
        assertEquals(2, state.menuSections.size)
        assertEquals(2, state.totalItemCount)
        assertNotNull(state.distanceKm)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadRestaurant error sets errorMessage`() = runTest {
        coEvery { getRestaurantDetailUseCase(999L) } returns GetRestaurantDetailResult.Error(FoodError.NETWORK_ERROR)

        viewModel.loadRestaurant(999L)

        val state = viewModel.uiState.value
        assertFalse(state.isRestaurantLoading)
        assertFalse(state.isMenuLoading)
        assertNull(state.restaurant)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `menu search filters sections correctly`() = runTest {
        coEvery { getRestaurantDetailUseCase(1L) } returns GetRestaurantDetailResult.Success(mockRestaurant)
        coEvery { getRestaurantItemsUseCase(1L, false) } returns GetMenuItemsResult.Success(listOf(mockMenuItem1, mockMenuItem2))
        viewModel.loadRestaurant(1L)

        viewModel.onMenuSearchQueryChanged("tra")
        val state = viewModel.uiState.value
        assertEquals("tra", state.menuSearchQuery)
        assertEquals(1, state.menuSections.size)
        assertEquals("Đồ uống", state.menuSections.first().category)
        assertEquals(102L, state.menuSections.first().items.first().id)
    }

    @Test
    fun `dish detail sheet open, change quantity and add to cart`() = runTest {
        coEvery { getRestaurantDetailUseCase(1L) } returns GetRestaurantDetailResult.Success(mockRestaurant)
        coEvery { getRestaurantItemsUseCase(1L, false) } returns GetMenuItemsResult.Success(listOf(mockMenuItem1))
        viewModel.loadRestaurant(1L)

        viewModel.onMenuItemClick(mockMenuItem1)
        var state = viewModel.uiState.value
        assertEquals(mockMenuItem1, state.selectedMenuItemForDetail)
        assertEquals(1, state.itemDetailQuantity)

        viewModel.onDetailQuantityChanged(2) // 1 + 2 = 3
        viewModel.onDetailNoteChanged("Ít bánh nhiều hành")
        state = viewModel.uiState.value
        assertEquals(3, state.itemDetailQuantity)
        assertEquals("Ít bánh nhiều hành", state.itemDetailNote)

        viewModel.onConfirmAddFromDetail()
        state = viewModel.uiState.value
        assertNull(state.selectedMenuItemForDetail)
        assertEquals(3, state.totalCartQuantity)
        assertEquals(150000.0, state.totalCartAmount, 0.01)
        assertEquals("Ít bánh nhiều hành", cartManager.cartState.value.items[101L]?.note)
    }

    @Test
    fun `cart operations add, update quantity and clear correctly via CartManager`() = runTest {
        coEvery { getRestaurantDetailUseCase(1L) } returns GetRestaurantDetailResult.Success(mockRestaurant)
        coEvery { getRestaurantItemsUseCase(1L, false) } returns GetMenuItemsResult.Success(listOf(mockMenuItem1, mockMenuItem2))
        viewModel.loadRestaurant(1L)

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

        // 3. Remove item 1
        viewModel.onRemoveFromCart(mockMenuItem1)
        state = viewModel.uiState.value
        assertEquals(1, state.totalCartQuantity)
        assertEquals(50000.0, state.totalCartAmount, 0.01)

        // 4. Clear cart
        viewModel.onClearCart()
        state = viewModel.uiState.value
        assertEquals(0, state.totalCartQuantity)
        assertEquals(0.0, state.totalCartAmount, 0.01)
        assertTrue(state.cartItems.isEmpty())
    }

    @Test
    fun `adding item from different restaurant triggers conflict dialog and confirms replacement`() = runTest {
        cartManager.addItem(mockMenuItem1, quantity = 1, restaurantName = "Phở Thìn")

        viewModel.onAddToCart(mockMenuItemOtherRestaurant)

        val state = viewModel.uiState.value
        assertNotNull(state.pendingConflict)
        assertEquals(1L, state.pendingConflict?.currentRestaurantId)
        assertEquals(2L, state.pendingConflict?.newRestaurantId)

        viewModel.onDismissConflictDialog()
        assertNull(viewModel.uiState.value.pendingConflict)

        viewModel.onAddToCart(mockMenuItemOtherRestaurant)
        viewModel.onConfirmReplaceCart()
        val replacedState = viewModel.uiState.value
        assertNull(replacedState.pendingConflict)
        assertEquals(1, replacedState.totalCartQuantity)
        assertEquals(60000.0, replacedState.totalCartAmount, 0.01)
    }
}
