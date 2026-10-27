package com.example.omnigo.features.customer.food.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.location.manager.UserLocationManager
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantsUseCase
import com.example.omnigo.features.customer.food.domain.util.GeoDistance
import com.example.omnigo.features.customer.food.domain.util.RestaurantListing
import com.example.omnigo.features.customer.food.domain.util.RestaurantSort
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val cartManager: CartManager,
    private val userLocationManager: UserLocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var currentLocation: UserLocation? = null

    init {
        loadRestaurants()
        observeSearch()
        observeCart()
        observeLocation()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartManager.cartState.collect { cart ->
                _uiState.update { current ->
                    current.copy(
                        cartRestaurantId = cart.restaurantId,
                        totalCartQuantity = cart.totalQuantity,
                        totalCartAmount = cart.totalAmount
                    )
                }
            }
        }
    }

    private fun observeLocation() {
        viewModelScope.launch {
            userLocationManager.currentLocation.collect { location ->
                currentLocation = location
                val label = location?.let { loc ->
                    loc.addressName.ifBlank { loc.fullAddress }.ifBlank { null }
                }
                _uiState.update { current ->
                    val distances = calculateDistances(current.restaurants, location)
                    val displayed = recomputeDisplayed(
                        restaurants = current.restaurants,
                        category = current.selectedCategory,
                        sort = current.selectedSort,
                        distances = distances
                    )
                    current.copy(
                        distancesKm = distances,
                        displayedRestaurants = displayed,
                        userAddressLabel = label
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun onCategorySelected(category: String?) {
        val newCategory = if (_uiState.value.selectedCategory == category) null else category
        _uiState.update { current ->
            val displayed = recomputeDisplayed(
                restaurants = current.restaurants,
                category = newCategory,
                sort = current.selectedSort,
                distances = current.distancesKm
            )
            current.copy(
                selectedCategory = newCategory,
                displayedRestaurants = displayed
            )
        }
    }

    fun onSortSelected(sort: RestaurantSort) {
        _uiState.update { current ->
            val displayed = recomputeDisplayed(
                restaurants = current.restaurants,
                category = current.selectedCategory,
                sort = sort,
                distances = current.distancesKm
            )
            current.copy(
                selectedSort = sort,
                displayedRestaurants = displayed
            )
        }
    }

    fun onClearFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedCategory = null) }
        searchQueryFlow.value = ""
        loadRestaurants(null)
    }

    fun loadRestaurants(query: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getRestaurantsUseCase(query)
            when (result) {
                is GetRestaurantsResult.Success -> {
                    val rawRestaurants = result.restaurants
                    val distances = calculateDistances(rawRestaurants, currentLocation)
                    val categories = RestaurantListing.deriveCategories(rawRestaurants)
                    val featured = RestaurantListing.featured(rawRestaurants)
                    val displayed = recomputeDisplayed(
                        restaurants = rawRestaurants,
                        category = _uiState.value.selectedCategory,
                        sort = _uiState.value.selectedSort,
                        distances = distances
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            restaurants = rawRestaurants,
                            displayedRestaurants = displayed,
                            featuredRestaurants = featured,
                            categories = categories,
                            distancesKm = distances
                        )
                    }
                }
                is GetRestaurantsResult.Error -> {
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

    private fun calculateDistances(
        restaurants: List<Restaurant>,
        location: UserLocation?
    ): Map<Long, Double> {
        if (location == null) return emptyMap()
        return restaurants.mapNotNull { restaurant ->
            if (restaurant.hasCoordinates) {
                val distance = GeoDistance.haversineKm(
                    location.latitude,
                    location.longitude,
                    restaurant.latitude,
                    restaurant.longitude
                )
                restaurant.id to ((distance * 10).toInt() / 10.0)
            } else {
                null
            }
        }.toMap()
    }

    private fun recomputeDisplayed(
        restaurants: List<Restaurant>,
        category: String?,
        sort: RestaurantSort,
        distances: Map<Long, Double>
    ): List<Restaurant> {
        val filtered = RestaurantListing.filterByCategory(restaurants, category)
        return RestaurantListing.sort(filtered, sort, distances)
    }

    private fun observeSearch() {
        searchQueryFlow
            .debounce(500L)
            .distinctUntilChanged()
            .onEach { query ->
                loadRestaurants(query.ifBlank { null })
            }
            .launchIn(viewModelScope)
    }
}
