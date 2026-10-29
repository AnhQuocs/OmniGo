package com.example.omnigo.features.customer.activity.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile

enum class ActivityTab {
    ONGOING,
    HISTORY
}

enum class HistoryServiceFilter {
    OMNI_RIDE,
    OMNI_FOOD
}

data class ActivityUiState(
    val isLoading: Boolean = false,
    val selectedTab: ActivityTab = ActivityTab.ONGOING,
    val selectedHistoryService: HistoryServiceFilter = HistoryServiceFilter.OMNI_FOOD,
    val ongoingBookings: List<Booking> = emptyList(),
    val historyBookings: List<Booking> = emptyList(),
    val ongoingFoodOrders: List<FoodOrder> = emptyList(),
    val historyFoodOrders: List<FoodOrder> = emptyList(),
    val driverProfiles: Map<Long, BookingDriverProfile> = emptyMap(),
    val activeRideState: ActiveRideState? = null,
    val cancelConfirmBookingId: Long? = null,
    val isCancellingBooking: Boolean = false,
    val errorMessage: String? = null
) {
    val ongoingCount: Int
        get() = ongoingBookings.size + ongoingFoodOrders.size

    val hasOngoing: Boolean
        get() = ongoingBookings.isNotEmpty() || ongoingFoodOrders.isNotEmpty()

    val hasHistory: Boolean
        get() = when (selectedHistoryService) {
            HistoryServiceFilter.OMNI_FOOD -> historyFoodOrders.isNotEmpty()
            HistoryServiceFilter.OMNI_RIDE -> historyBookings.isNotEmpty()
        }
}
