package com.example.omnigo.features.customer.food.domain.manager

import com.example.omnigo.features.customer.food.domain.model.CartAddResult
import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.domain.model.CartState
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartManager @Inject constructor() {

    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    fun addItem(
        menuItem: MenuItem,
        quantity: Int = 1,
        note: String = "",
        restaurantName: String = ""
    ): CartAddResult {
        if (!menuItem.isAvailable || quantity <= 0) {
            return CartAddResult.ItemUnavailable
        }

        val currentState = _cartState.value
        if (currentState.restaurantId != null &&
            currentState.restaurantId != menuItem.restaurantId &&
            currentState.items.isNotEmpty()
        ) {
            return CartAddResult.RestaurantConflict(
                currentRestaurantId = currentState.restaurantId,
                currentRestaurantName = currentState.restaurantName,
                newRestaurantId = menuItem.restaurantId,
                newRestaurantName = restaurantName,
                pendingItem = menuItem,
                pendingQuantity = quantity,
                pendingNote = note
            )
        }

        _cartState.update { state ->
            val updatedItems = state.items.toMutableMap()
            val existing = updatedItems[menuItem.id]
            val newQty = (existing?.quantity ?: 0) + quantity
            val newNote = if (note.isNotBlank()) note else (existing?.note.orEmpty())
            updatedItems[menuItem.id] = CartItem(
                menuItem = menuItem,
                quantity = newQty,
                note = newNote
            )
            state.copy(
                restaurantId = menuItem.restaurantId,
                restaurantName = if (restaurantName.isNotBlank()) restaurantName else state.restaurantName,
                items = updatedItems
            )
        }

        return CartAddResult.Success
    }

    fun updateQuantity(menuItemId: Long, quantity: Int) {
        _cartState.update { state ->
            if (quantity <= 0) {
                val updatedItems = state.items.toMutableMap()
                updatedItems.remove(menuItemId)
                if (updatedItems.isEmpty()) {
                    CartState()
                } else {
                    state.copy(items = updatedItems)
                }
            } else {
                val item = state.items[menuItemId] ?: return@update state
                val updatedItems = state.items.toMutableMap()
                updatedItems[menuItemId] = item.copy(quantity = quantity)
                state.copy(items = updatedItems)
            }
        }
    }

    fun removeItem(menuItemId: Long) {
        updateQuantity(menuItemId, 0)
    }

    fun updateNote(menuItemId: Long, note: String) {
        _cartState.update { state ->
            val item = state.items[menuItemId] ?: return@update state
            val updatedItems = state.items.toMutableMap()
            updatedItems[menuItemId] = item.copy(note = note)
            state.copy(items = updatedItems)
        }
    }

    fun clearCart() {
        _cartState.value = CartState()
    }

    fun replaceCartWithItem(
        menuItem: MenuItem,
        quantity: Int = 1,
        note: String = "",
        restaurantName: String = ""
    ): CartAddResult {
        if (!menuItem.isAvailable || quantity <= 0) {
            return CartAddResult.ItemUnavailable
        }

        _cartState.value = CartState(
            restaurantId = menuItem.restaurantId,
            restaurantName = restaurantName,
            items = mapOf(
                menuItem.id to CartItem(
                    menuItem = menuItem,
                    quantity = quantity,
                    note = note
                )
            )
        )
        return CartAddResult.Success
    }

    /**
     * Khôi phục toàn bộ giỏ hàng từ một đơn hàng trước đó (phục vụ chức năng Đặt lại - Reorder).
     */
    fun restoreCartFromOrder(order: FoodOrder) {
        if (order.items.isEmpty()) return

        val restoredMap = order.items.associate { item ->
            val menuItem = MenuItem(
                id = item.menuItemId,
                restaurantId = order.restaurantId,
                name = item.itemName,
                description = "",
                price = item.itemPrice,
                imageUrl = item.imageUrl.orEmpty(),
                category = "",
                isAvailable = true
            )
            item.menuItemId to CartItem(
                menuItem = menuItem,
                quantity = item.quantity.coerceAtLeast(1),
                note = item.note.orEmpty()
            )
        }

        _cartState.value = CartState(
            restaurantId = order.restaurantId,
            restaurantName = order.restaurantName,
            items = restoredMap
        )
    }

    fun getItemQuantity(menuItemId: Long): Int {
        return _cartState.value.items[menuItemId]?.quantity ?: 0
    }
}
