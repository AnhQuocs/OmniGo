package com.example.omnigo.features.customer.food.domain.manager

import com.example.omnigo.features.customer.food.domain.model.CartAddResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartManagerTest {

    private lateinit var cartManager: CartManager

    private val itemRestaurant1A = MenuItem(
        id = 101L,
        restaurantId = 1L,
        name = "Phở Tái",
        description = "Bò tươi mềm",
        price = 50000.0,
        imageUrl = "https://example.com/pho.jpg",
        category = "Phở",
        isAvailable = true
    )

    private val itemRestaurant1B = MenuItem(
        id = 102L,
        restaurantId = 1L,
        name = "Trà Đá",
        description = "Mát lạnh",
        price = 5000.0,
        imageUrl = "https://example.com/tra.jpg",
        category = "Đồ uống",
        isAvailable = true
    )

    private val unavailableItem = MenuItem(
        id = 103L,
        restaurantId = 1L,
        name = "Phở Đặc Biệt (Hết)",
        description = "Hết hàng",
        price = 80000.0,
        imageUrl = "https://example.com/pho_db.jpg",
        category = "Phở",
        isAvailable = false
    )

    private val itemRestaurant2 = MenuItem(
        id = 201L,
        restaurantId = 2L,
        name = "Bún Chả",
        description = "Chả nướng than hoa",
        price = 60000.0,
        imageUrl = "https://example.com/buncha.jpg",
        category = "Bún",
        isAvailable = true
    )

    @Before
    fun setUp() {
        cartManager = CartManager()
    }

    @Test
    fun `initial cart state is empty`() {
        val state = cartManager.cartState.value
        assertNull(state.restaurantId)
        assertEquals("", state.restaurantName)
        assertTrue(state.items.isEmpty())
        assertEquals(0, state.totalQuantity)
        assertEquals(0.0, state.totalAmount, 0.001)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `addItem adds item and updates cart state`() {
        val result = cartManager.addItem(
            menuItem = itemRestaurant1A,
            quantity = 2,
            note = "Không hành",
            restaurantName = "Phở Thìn"
        )

        assertTrue(result is CartAddResult.Success)
        val state = cartManager.cartState.value
        assertEquals(1L, state.restaurantId)
        assertEquals("Phở Thìn", state.restaurantName)
        assertEquals(1, state.items.size)
        assertEquals(2, state.totalQuantity)
        assertEquals(100000.0, state.totalAmount, 0.001)
        assertFalse(state.isEmpty)

        val cartItem = state.items[101L]
        assertEquals(2, cartItem?.quantity)
        assertEquals("Không hành", cartItem?.note)
        assertEquals(100000.0, cartItem?.subtotalPrice ?: 0.0, 0.001)
    }

    @Test
    fun `addItem with unavailable item returns ItemUnavailable and does not modify cart`() {
        val result = cartManager.addItem(menuItem = unavailableItem)

        assertTrue(result is CartAddResult.ItemUnavailable)
        val state = cartManager.cartState.value
        assertTrue(state.isEmpty)
        assertEquals(0, state.totalQuantity)
    }

    @Test
    fun `addItem with zero or negative quantity returns ItemUnavailable`() {
        val result = cartManager.addItem(menuItem = itemRestaurant1A, quantity = 0)
        assertTrue(result is CartAddResult.ItemUnavailable)
        assertTrue(cartManager.cartState.value.isEmpty)
    }

    @Test
    fun `addItem for same restaurant increments quantity and preserves or updates note`() {
        cartManager.addItem(itemRestaurant1A, quantity = 1, note = "Ít bánh", restaurantName = "Phở Thìn")
        cartManager.addItem(itemRestaurant1A, quantity = 2)

        val state = cartManager.cartState.value
        assertEquals(3, state.totalQuantity)
        assertEquals(150000.0, state.totalAmount, 0.001)
        assertEquals("Ít bánh", state.items[101L]?.note)

        // Adding another item from same restaurant
        cartManager.addItem(itemRestaurant1B, quantity = 2, restaurantName = "Phở Thìn")
        val updatedState = cartManager.cartState.value
        assertEquals(2, updatedState.items.size)
        assertEquals(5, updatedState.totalQuantity)
        assertEquals(160000.0, updatedState.totalAmount, 0.001)
    }

    @Test
    fun `addItem from different restaurant triggers RestaurantConflict`() {
        cartManager.addItem(itemRestaurant1A, quantity = 1, restaurantName = "Phở Thìn")

        val result = cartManager.addItem(
            menuItem = itemRestaurant2,
            quantity = 1,
            note = "Nhiều ớt",
            restaurantName = "Bún Chả Hàng Quạt"
        )

        assertTrue(result is CartAddResult.RestaurantConflict)
        val conflict = result as CartAddResult.RestaurantConflict
        assertEquals(1L, conflict.currentRestaurantId)
        assertEquals("Phở Thìn", conflict.currentRestaurantName)
        assertEquals(2L, conflict.newRestaurantId)
        assertEquals("Bún Chả Hàng Quạt", conflict.newRestaurantName)
        assertEquals(itemRestaurant2, conflict.pendingItem)
        assertEquals(1, conflict.pendingQuantity)
        assertEquals("Nhiều ớt", conflict.pendingNote)

        // Cart items should still belong to restaurant 1
        assertEquals(1L, cartManager.cartState.value.restaurantId)
        assertEquals(1, cartManager.cartState.value.items.size)
    }

    @Test
    fun `replaceCartWithItem clears old restaurant items and sets new item`() {
        cartManager.addItem(itemRestaurant1A, quantity = 2, restaurantName = "Phở Thìn")

        val replaceResult = cartManager.replaceCartWithItem(
            menuItem = itemRestaurant2,
            quantity = 1,
            note = "Nhiều giấm tỏi",
            restaurantName = "Bún Chả Hàng Quạt"
        )

        assertTrue(replaceResult is CartAddResult.Success)
        val state = cartManager.cartState.value
        assertEquals(2L, state.restaurantId)
        assertEquals("Bún Chả Hàng Quạt", state.restaurantName)
        assertEquals(1, state.items.size)
        assertEquals(1, state.totalQuantity)
        assertEquals(60000.0, state.totalAmount, 0.001)
        assertEquals("Nhiều giấm tỏi", state.items[201L]?.note)
    }

    @Test
    fun `updateQuantity increases, decreases and removes item when zero`() {
        cartManager.addItem(itemRestaurant1A, quantity = 2, restaurantName = "Phở Thìn")

        // Increase quantity
        cartManager.updateQuantity(101L, 4)
        assertEquals(4, cartManager.cartState.value.totalQuantity)
        assertEquals(200000.0, cartManager.cartState.value.totalAmount, 0.001)

        // Decrease quantity
        cartManager.updateQuantity(101L, 1)
        assertEquals(1, cartManager.cartState.value.totalQuantity)
        assertEquals(50000.0, cartManager.cartState.value.totalAmount, 0.001)

        // Decrease to 0 removes item and resets cart state
        cartManager.updateQuantity(101L, 0)
        assertTrue(cartManager.cartState.value.isEmpty)
        assertNull(cartManager.cartState.value.restaurantId)
    }

    @Test
    fun `removeItem removes item and resets cart when no items left`() {
        cartManager.addItem(itemRestaurant1A, quantity = 1, restaurantName = "Phở Thìn")
        cartManager.addItem(itemRestaurant1B, quantity = 1, restaurantName = "Phở Thìn")

        cartManager.removeItem(101L)
        assertEquals(1, cartManager.cartState.value.items.size)
        assertEquals(1, cartManager.cartState.value.totalQuantity)
        assertEquals(1L, cartManager.cartState.value.restaurantId)

        cartManager.removeItem(102L)
        assertTrue(cartManager.cartState.value.isEmpty)
        assertNull(cartManager.cartState.value.restaurantId)
    }

    @Test
    fun `updateNote updates note for existing item`() {
        cartManager.addItem(itemRestaurant1A, quantity = 1, restaurantName = "Phở Thìn")
        cartManager.updateNote(101L, "Giao trước 12h")

        assertEquals("Giao trước 12h", cartManager.cartState.value.items[101L]?.note)
    }

    @Test
    fun `clearCart clears all items and resets restaurant`() {
        cartManager.addItem(itemRestaurant1A, quantity = 3, restaurantName = "Phở Thìn")
        assertFalse(cartManager.cartState.value.isEmpty)

        cartManager.clearCart()
        assertTrue(cartManager.cartState.value.isEmpty)
        assertNull(cartManager.cartState.value.restaurantId)
        assertEquals(0, cartManager.cartState.value.totalQuantity)
    }

    @Test
    fun `replaceCartWithItem with unavailable item or non-positive quantity returns ItemUnavailable`() {
        val resultUnavailable = cartManager.replaceCartWithItem(unavailableItem, quantity = 1)
        assertTrue(resultUnavailable is CartAddResult.ItemUnavailable)

        val resultZeroQty = cartManager.replaceCartWithItem(itemRestaurant2, quantity = 0)
        assertTrue(resultZeroQty is CartAddResult.ItemUnavailable)
    }

    @Test
    fun `updateQuantity and updateNote on non-existent item do not affect cart state`() {
        cartManager.addItem(itemRestaurant1A, quantity = 2, restaurantName = "Phở Thìn")

        cartManager.updateQuantity(999L, 5)
        assertEquals(2, cartManager.cartState.value.totalQuantity)

        cartManager.updateNote(999L, "Ghi chú lạ")
        assertEquals(2, cartManager.cartState.value.totalQuantity)
        assertNull(cartManager.cartState.value.items[999L])
    }

    @Test
    fun `getItemQuantity returns correct quantity`() {
        assertEquals(0, cartManager.getItemQuantity(101L))
        cartManager.addItem(itemRestaurant1A, quantity = 3)
        assertEquals(3, cartManager.getItemQuantity(101L))
        assertEquals(0, cartManager.getItemQuantity(999L))
    }
}
