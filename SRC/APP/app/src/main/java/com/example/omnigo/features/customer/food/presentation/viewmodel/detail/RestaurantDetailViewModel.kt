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
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantReviewsUseCase
import com.example.omnigo.features.customer.food.domain.util.GeoDistance
import com.example.omnigo.features.customer.food.domain.util.MenuSectionBuilder
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val getRestaurantItemsUseCase: GetRestaurantItemsUseCase,
    private val getRestaurantReviewsUseCase: GetRestaurantReviewsUseCase,
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

            // 1. Lấy thông tin nhà hàng
            when (val result = getRestaurantDetailUseCase(id)) {
                is GetRestaurantDetailResult.Success -> {
                    val restaurant = result.restaurant
                    val distance = calculateDistanceKm(restaurant)

                    _uiState.update {
                        it.copy(
                            isRestaurantLoading = false,
                            restaurant = restaurant,
                            distanceKm = distance
                        )
                    }

                    // 2. Lấy thông tin Menu Items (lấy cả món hết hàng để thông tin đầy đủ, rõ ràng)
                    when (val menuResult = getRestaurantItemsUseCase(id, availableOnly = false)) {
                        is GetMenuItemsResult.Success -> {
                            val items = menuResult.items
                            val categories = items
                                .map { it.category.trim() }
                                .filter { it.isNotBlank() }
                                .distinct()
                            val sections = MenuSectionBuilder.build(items, _uiState.value.menuSearchQuery)

                            _uiState.update {
                                it.copy(
                                    isMenuLoading = false,
                                    rawMenuItems = items,
                                    categories = categories,
                                    menuSections = sections
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
            loadReviews(id)
        }
    }

    fun onMenuSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val sections = MenuSectionBuilder.build(current.rawMenuItems, query)
            current.copy(
                menuSearchQuery = query,
                menuSections = sections
            )
        }
    }

    fun loadReviews(restaurantId: Long, rating: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewsLoading = true, selectedRatingFilter = rating) }
            val reviewsResult = getRestaurantReviewsUseCase(
                restaurantId = restaurantId,
                page = 0,
                size = 20,
                rating = rating
            )
            reviewsResult.fold(
                onSuccess = { reviews ->
                    _uiState.update { it.copy(isReviewsLoading = false, reviews = reviews) }
                },
                onFailure = {
                    _uiState.update { it.copy(isReviewsLoading = false) }
                }
            )
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onMenuItemClick(menuItem: MenuItem) {
        val existingNote = cartManager.cartState.value.items[menuItem.id]?.note.orEmpty()
        _uiState.update {
            it.copy(
                selectedMenuItemForDetail = menuItem,
                itemDetailQuantity = 1,
                itemDetailNote = existingNote
            )
        }
    }

    fun onDismissMenuItemDetail() {
        _uiState.update { it.copy(selectedMenuItemForDetail = null) }
    }

    fun onDetailQuantityChanged(delta: Int) {
        _uiState.update { current ->
            val newQty = (current.itemDetailQuantity + delta).coerceIn(1, 99)
            current.copy(itemDetailQuantity = newQty)
        }
    }

    fun onDetailNoteChanged(note: String) {
        _uiState.update { it.copy(itemDetailNote = note) }
    }

    fun onConfirmAddFromDetail() {
        val current = _uiState.value
        val menuItem = current.selectedMenuItemForDetail ?: return
        if (current.restaurant?.canOrder == false || !menuItem.isAvailable) return

        val restaurantName = current.restaurant?.name.orEmpty()
        val result = cartManager.addItem(
            menuItem = menuItem,
            quantity = current.itemDetailQuantity,
            note = current.itemDetailNote.trim(),
            restaurantName = restaurantName
        )
        if (result is CartAddResult.RestaurantConflict) {
            _uiState.update { it.copy(pendingConflict = result, selectedMenuItemForDetail = null) }
        } else {
            _uiState.update { it.copy(selectedMenuItemForDetail = null) }
        }
    }

    fun onAddToCart(menuItem: MenuItem) {
        val restaurant = _uiState.value.restaurant
        if (restaurant != null && !restaurant.canOrder) return
        if (!menuItem.isAvailable) return

        val restaurantName = restaurant?.name.orEmpty()
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

    private fun calculateDistanceKm(restaurant: Restaurant): Double? {
        val userLoc = userLocationManager.currentLocation.value ?: return null
        if (!restaurant.hasCoordinates) return null
        val distance = GeoDistance.haversineKm(
            userLoc.latitude,
            userLoc.longitude,
            restaurant.latitude,
            restaurant.longitude
        )
        return (distance * 10).toInt() / 10.0
    }
}
