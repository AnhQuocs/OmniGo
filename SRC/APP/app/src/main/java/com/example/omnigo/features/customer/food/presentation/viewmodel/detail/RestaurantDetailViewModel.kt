package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.CartAddResult
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantItemsUseCase
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val getRestaurantItemsUseCase: GetRestaurantItemsUseCase,
    private val userLocationManager: UserLocationManager,
    private val cartManager: CartManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantDetailUiState())
    val uiState: StateFlow<RestaurantDetailUiState> = _uiState.asStateFlow()

    init {
        observeCartState()
    }

    private fun observeCartState() {
        viewModelScope.launch {
            cartManager.cartState.collect { cart ->
                _uiState.update { current ->
                    current.copy(
                        cartItems = cart.items,
                        totalCartAmount = cart.totalAmount,
                        totalCartQuantity = cart.totalQuantity
                    )
                }
            }
        }
    }

    fun loadRestaurant(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestaurantLoading = true, isMenuLoading = true, errorMessage = null) }
            
            // 1. Lấy thông tin nhà hàng trước
            when (val result = getRestaurantDetailUseCase(id)) {
                is GetRestaurantDetailResult.Success -> {
                    val restaurant = result.restaurant
                    val (distance, minutes) = calculateDistanceAndMinutes(restaurant)

                    _uiState.update {
                        it.copy(
                            isRestaurantLoading = false,
                            restaurant = restaurant,
                            distanceKm = distance,
                            estimatedDeliveryMinutes = minutes
                        )
                    }
                    
                    // 2. Lấy thông tin Menu Items
                    when (val menuResult = getRestaurantItemsUseCase(id, availableOnly = false)) {
                        is GetMenuItemsResult.Success -> {
                            val items = menuResult.items
                            val categories = extractCategories(items)
                            val groupedMenu = items.groupBy { it.category.trim() }.filterKeys { it.isNotBlank() }
                            
                            _uiState.update {
                                it.copy(
                                    isMenuLoading = false,
                                    categories = categories,
                                    menuItems = groupedMenu
                                )
                            }
                        }
                        is GetMenuItemsResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isMenuLoading = false,
                                    errorMessage = menuResult.error.asUiText()
                                )
                            }
                        }
                    }
                }
                is GetRestaurantDetailResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRestaurantLoading = false,
                            isMenuLoading = false,
                            errorMessage = result.error.asUiText()
                        )
                    }
                }
            }
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onAddToCart(menuItem: MenuItem) {
        val restaurantName = _uiState.value.restaurant?.name.orEmpty()
        val result = cartManager.addItem(
            menuItem = menuItem,
            quantity = 1,
            restaurantName = restaurantName
        )
        if (result is CartAddResult.RestaurantConflict) {
            _uiState.update { it.copy(pendingConflict = result) }
        }
    }

    fun onRemoveFromCart(menuItem: MenuItem) {
        val currentQty = cartManager.getItemQuantity(menuItem.id)
        if (currentQty > 1) {
            cartManager.updateQuantity(menuItem.id, currentQty - 1)
        } else {
            cartManager.removeItem(menuItem.id)
        }
    }

    fun onConfirmReplaceCart() {
        val conflict = _uiState.value.pendingConflict ?: return
        cartManager.replaceCartWithItem(
            menuItem = conflict.pendingItem,
            quantity = conflict.pendingQuantity,
            note = conflict.pendingNote,
            restaurantName = conflict.newRestaurantName
        )
        _uiState.update { it.copy(pendingConflict = null) }
    }

    fun onDismissConflictDialog() {
        _uiState.update { it.copy(pendingConflict = null) }
    }

    fun onUpdateItemNote(menuItemId: Long, note: String) {
        cartManager.updateNote(menuItemId, note)
    }

    fun onClearCart() {
        cartManager.clearCart()
    }

    private fun extractCategories(items: List<MenuItem>): List<String> {
        return items
            .map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun calculateDistanceAndMinutes(restaurant: Restaurant): Pair<Double?, Int?> {
        val userLoc = userLocationManager.currentLocation.value ?: return Pair(null, null)
        if (restaurant.latitude == 0.0 && restaurant.longitude == 0.0) return Pair(null, null)

        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(restaurant.latitude - userLoc.latitude)
        val dLon = Math.toRadians(restaurant.longitude - userLoc.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLoc.latitude)) * cos(Math.toRadians(restaurant.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = (r * c * 10).toInt() / 10.0 // 1 decimal place

        val minutes = (15 + (distance * 3.5)).toInt().coerceAtLeast(15)
        return Pair(distance, minutes)
    }
}
