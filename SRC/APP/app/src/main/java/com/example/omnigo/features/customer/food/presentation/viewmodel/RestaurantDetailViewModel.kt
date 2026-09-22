package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
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
    private val userLocationManager: UserLocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantDetailUiState())
    val uiState: StateFlow<RestaurantDetailUiState> = _uiState.asStateFlow()

    fun loadRestaurant(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getRestaurantDetailUseCase(id)) {
                is GetRestaurantDetailResult.Success -> {
                    val restaurant = result.restaurant
                    val categories = extractCategories(restaurant)
                    val (distance, minutes) = calculateDistanceAndMinutes(restaurant)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            restaurant = restaurant,
                            categories = categories,
                            selectedCategory = null,
                            distanceKm = distance,
                            estimatedDeliveryMinutes = minutes
                        )
                    }
                }
                is GetRestaurantDetailResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
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
        _uiState.update { state ->
            val currentCart = state.cartItems.toMutableMap()
            val existing = currentCart[menuItem.id]
            val newQuantity = (existing?.quantity ?: 0) + 1
            currentCart[menuItem.id] = CartItem(menuItem, newQuantity)

            val totalAmount = currentCart.values.sumOf { it.menuItem.price * it.quantity }
            val totalCount = currentCart.values.sumOf { it.quantity }

            state.copy(
                cartItems = currentCart,
                totalCartAmount = totalAmount,
                totalCartQuantity = totalCount
            )
        }
    }

    fun onRemoveFromCart(menuItem: MenuItem) {
        _uiState.update { state ->
            val currentCart = state.cartItems.toMutableMap()
            val existing = currentCart[menuItem.id]
            if (existing != null) {
                if (existing.quantity > 1) {
                    currentCart[menuItem.id] = CartItem(menuItem, existing.quantity - 1)
                } else {
                    currentCart.remove(menuItem.id)
                }
            }

            val totalAmount = currentCart.values.sumOf { it.menuItem.price * it.quantity }
            val totalCount = currentCart.values.sumOf { it.quantity }

            state.copy(
                cartItems = currentCart,
                totalCartAmount = totalAmount,
                totalCartQuantity = totalCount
            )
        }
    }

    fun onClearCart() {
        _uiState.update {
            it.copy(
                cartItems = emptyMap(),
                totalCartAmount = 0.0,
                totalCartQuantity = 0
            )
        }
    }

    private fun extractCategories(restaurant: Restaurant): List<String> {
        return restaurant.menuItems
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
