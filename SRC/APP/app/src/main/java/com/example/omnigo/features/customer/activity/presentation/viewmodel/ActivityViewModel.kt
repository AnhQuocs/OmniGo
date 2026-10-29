package com.example.omnigo.features.customer.activity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.activity.domain.util.ActivityHistoryHelper
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetMyFoodOrdersUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantItemsUseCase
import com.example.omnigo.features.customer.food.domain.util.FoodOrderHistoryHelper
import com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetCustomerBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val getCustomerBookingsUseCase: GetCustomerBookingsUseCase,
    private val getBookingDriverUseCase: GetBookingDriverUseCase,
    private val cancelRideBookingUseCase: CancelRideBookingUseCase,
    private val rideTrackingSessionManager: RideTrackingSessionManager,
    private val getMyFoodOrdersUseCase: GetMyFoodOrdersUseCase,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val getRestaurantItemsUseCase: GetRestaurantItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private val restaurantCache = mutableMapOf<Long, Restaurant>()
    private val menuCatalogCache = mutableMapOf<Long, List<MenuItem>>()

    init {
        observeActiveRideSession()
        loadActivities()
    }

    private fun observeActiveRideSession() {
        viewModelScope.launch {
            rideTrackingSessionManager.activeRideState.collect { activeRide ->
                _uiState.update { it.copy(activeRideState = activeRide) }
            }
        }
    }

    fun onTabSelected(tab: ActivityTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onHistoryServiceSelected(service: HistoryServiceFilter) {
        _uiState.update { it.copy(selectedHistoryService = service) }
    }

    fun loadBookings() {
        loadActivities()
    }

    fun loadActivities(isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            coroutineScope {
                val bookingsDeferred = async { getCustomerBookingsUseCase() }
                val foodOrdersDeferred = async { getMyFoodOrdersUseCase() }

                val bookingsResult = bookingsDeferred.await()
                val foodOrdersResult = foodOrdersDeferred.await()

                var ongoingRideList = _uiState.value.ongoingBookings
                var historyRideList = _uiState.value.historyBookings
                var ongoingFoodList = _uiState.value.ongoingFoodOrders
                var historyFoodList = _uiState.value.historyFoodOrders
                var errorMsg: String? = null

                when (bookingsResult) {
                    is GetCustomerBookingsResult.Success -> {
                        ongoingRideList = bookingsResult.bookings.filter {
                            it.status == BookingStatus.PENDING ||
                                it.status == BookingStatus.ACCEPTED ||
                                it.status == BookingStatus.ARRIVED ||
                                it.status == BookingStatus.IN_PROGRESS
                        }
                        historyRideList = bookingsResult.bookings.filter {
                            it.status == BookingStatus.COMPLETED ||
                                it.status == BookingStatus.CANCELLED
                        }
                    }
                    is GetCustomerBookingsResult.Error -> {
                        errorMsg = bookingsResult.message
                    }
                }

                var allEnrichedFoodOrders: List<FoodOrder> = emptyList()
                when (foodOrdersResult) {
                    is GetMyFoodOrdersResult.Success -> {
                        allEnrichedFoodOrders = applyCachedEnrichment(foodOrdersResult.orders)
                        ongoingFoodList = allEnrichedFoodOrders.filter(ActivityHistoryHelper::isOngoingFoodOrder)
                        historyFoodList = allEnrichedFoodOrders.filter(ActivityHistoryHelper::isHistoryFoodOrder)
                    }
                    is GetMyFoodOrdersResult.Error -> {
                        if (errorMsg == null) {
                            errorMsg = foodOrdersResult.message
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        ongoingBookings = ongoingRideList,
                        historyBookings = historyRideList,
                        ongoingFoodOrders = ongoingFoodList,
                        historyFoodOrders = historyFoodList,
                        errorMessage = errorMsg
                    )
                }

                if (ongoingRideList.isNotEmpty()) {
                    fetchOngoingDriverProfiles(ongoingRideList)
                }
                if (allEnrichedFoodOrders.isNotEmpty()) {
                    enrichOrdersWithRestaurantData(allEnrichedFoodOrders)
                }
            }
        }
    }

    fun promptCancelBooking(bookingId: Long) {
        _uiState.update { it.copy(cancelConfirmBookingId = bookingId) }
    }

    fun dismissCancelBookingDialog() {
        _uiState.update { it.copy(cancelConfirmBookingId = null) }
    }

    fun confirmCancelBooking() {
        val bookingId = _uiState.value.cancelConfirmBookingId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancellingBooking = true) }
            when (val result = cancelRideBookingUseCase(bookingId)) {
                is CancelRideBookingResult.Success -> {
                    if (rideTrackingSessionManager.activeRideState.value?.bookingId == bookingId) {
                        rideTrackingSessionManager.clearSession()
                    }
                    _uiState.update {
                        it.copy(
                            isCancellingBooking = false,
                            cancelConfirmBookingId = null
                        )
                    }
                    loadActivities(isSilent = true)
                }
                is CancelRideBookingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCancellingBooking = false,
                            cancelConfirmBookingId = null,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun fetchOngoingDriverProfiles(ongoingBookings: List<Booking>) {
        val bookingsWithDrivers = ongoingBookings.filter {
            it.driverId != null && it.driverId > 0L && !_uiState.value.driverProfiles.containsKey(it.bookingId)
        }
        if (bookingsWithDrivers.isEmpty()) return

        viewModelScope.launch {
            val newProfiles = _uiState.value.driverProfiles.toMutableMap()
            for (booking in bookingsWithDrivers) {
                when (val res = getBookingDriverUseCase(booking.bookingId)) {
                    is GetBookingDriverResult.Success -> {
                        newProfiles[booking.bookingId] = res.driverProfile
                    }
                    is GetBookingDriverResult.Error -> Unit
                }
            }
            _uiState.update { it.copy(driverProfiles = newProfiles) }
        }
    }

    private fun applyCachedEnrichment(orders: List<FoodOrder>): List<FoodOrder> {
        return orders.map { order ->
            val restaurant = restaurantCache[order.restaurantId]
            val catalog = menuCatalogCache[order.restaurantId].orEmpty()
            if (restaurant != null || catalog.isNotEmpty()) {
                FoodOrderHistoryHelper.enrichOrder(order, restaurant, catalog)
            } else {
                order
            }
        }
    }

    private fun enrichOrdersWithRestaurantData(orders: List<FoodOrder>) {
        val restaurantIdsToFetch = orders
            .map { it.restaurantId }
            .filter { it > 0L }
            .distinct()
            .filter { id ->
                !restaurantCache.containsKey(id) ||
                    orders.any { o ->
                        o.restaurantId == id &&
                            (o.restaurantImageUrl.isNullOrBlank() ||
                                o.restaurantAddress.isNullOrBlank() ||
                                o.items.any { it.imageUrl.isNullOrBlank() })
                    }
            }

        if (restaurantIdsToFetch.isEmpty()) return

        viewModelScope.launch {
            for (restaurantId in restaurantIdsToFetch) {
                if (!restaurantCache.containsKey(restaurantId)) {
                    when (val res = getRestaurantDetailUseCase(restaurantId)) {
                        is GetRestaurantDetailResult.Success -> {
                            restaurantCache[restaurantId] = res.restaurant
                            if (res.restaurant.menuItems.isNotEmpty()) {
                                menuCatalogCache[restaurantId] = res.restaurant.menuItems
                            }
                        }
                        else -> Unit
                    }
                }

                val currentCatalog = menuCatalogCache[restaurantId].orEmpty()
                if (currentCatalog.isEmpty() || currentCatalog.none { it.imageUrl.isNotBlank() }) {
                    when (val menuRes = getRestaurantItemsUseCase(restaurantId, availableOnly = false)) {
                        is GetMenuItemsResult.Success -> {
                            menuCatalogCache[restaurantId] = menuRes.items
                        }
                        else -> Unit
                    }
                }
            }

            _uiState.update { current ->
                val enrichedOngoing = applyCachedEnrichment(current.ongoingFoodOrders)
                val enrichedHistory = applyCachedEnrichment(current.historyFoodOrders)
                current.copy(
                    ongoingFoodOrders = enrichedOngoing,
                    historyFoodOrders = enrichedHistory
                )
            }
        }
    }
}
