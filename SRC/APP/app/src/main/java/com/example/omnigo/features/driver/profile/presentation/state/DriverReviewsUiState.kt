package com.example.omnigo.features.driver.profile.presentation.state

import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.ride.domain.model.BookingReview

data class DriverReviewsUiState(
    val selectedTabIndex: Int = 0, // 0 = Ride Reviews, 1 = Food Delivery Reviews
    val selectedStarFilter: Int = 0, // 0 = All, 5..1 = specific star
    val isLoading: Boolean = false,
    val rideTotalReviews: Int = 0,
    val rideAverageRating: Double = 0.0,
    val rideReviews: List<BookingReview> = emptyList(),
    val foodTotalReviews: Int = 0,
    val foodAverageRating: Double = 0.0,
    val foodReviews: List<FoodReview> = emptyList(),
    val errorMessage: String? = null
) {
    val currentAverageRating: Double
        get() = if (selectedTabIndex == 0) rideAverageRating else foodAverageRating

    val currentTotalReviews: Int
        get() = if (selectedTabIndex == 0) rideTotalReviews else foodTotalReviews

    val filteredRideReviews: List<BookingReview>
        get() = if (selectedStarFilter == 0) rideReviews else rideReviews.filter { it.rating == selectedStarFilter }

    val filteredFoodReviews: List<FoodReview>
        get() = if (selectedStarFilter == 0) foodReviews else foodReviews.filter { it.driverRating == selectedStarFilter }

    fun starCountForCurrentTab(star: Int): Int {
        return if (selectedTabIndex == 0) {
            rideReviews.count { it.rating == star }
        } else {
            foodReviews.count { it.driverRating == star }
        }
    }
}
