package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.CartAddResult
import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.util.MenuSection
import com.example.omnigo.utils.UiText

data class RestaurantDetailUiState(
    val isRestaurantLoading: Boolean = false,
    val isMenuLoading: Boolean = false,
    val restaurant: Restaurant? = null,
    val errorMessage: UiText? = null,
    val rawMenuItems: List<MenuItem> = emptyList(),
    val menuSections: List<MenuSection> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val menuSearchQuery: String = "",
    val cartItems: Map<Long, CartItem> = emptyMap(),
    val totalCartAmount: Double = 0.0,
    val totalCartQuantity: Int = 0,
    val distanceKm: Double? = null,
    val pendingConflict: CartAddResult.RestaurantConflict? = null,
    val reviews: List<FoodReview> = emptyList(),
    val isReviewsLoading: Boolean = false,
    val selectedRatingFilter: Int? = null,
    val selectedMenuItemForDetail: MenuItem? = null,
    val itemDetailQuantity: Int = 1,
    val itemDetailNote: String = ""
) {
    val canOrder: Boolean get() = restaurant?.canOrder == true

    /** Danh sách món hiển thị theo category hoặc tất cả khi selectedCategory rỗng */
    val displayedSections: List<MenuSection>
        get() = if (selectedCategory.isNullOrBlank()) {
            menuSections
        } else {
            menuSections.filter { it.category == selectedCategory }
        }

    val totalItemCount: Int
        get() = displayedSections.sumOf { it.items.size }
}
