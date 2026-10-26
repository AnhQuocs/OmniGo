package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.usecase.SearchPlacesUseCase
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class RidePlaceSearchCoordinator @Inject constructor(
    private val searchPlacesUseCase: SearchPlacesUseCase
) {
    private var searchJob: Job? = null

    fun openPlaceSearch(
        target: LocationTarget,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
    ) {
        searchJob?.cancel()
        updateState {
            it.copy(
                isSearchingPlaces = true,
                searchTarget = target,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchingPlacesLoading = false
            )
        }
    }

    fun closePlaceSearch(
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
    ) {
        searchJob?.cancel()
        updateState {
            it.copy(
                isSearchingPlaces = false,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchingPlacesLoading = false
            )
        }
    }

    fun updateSearchQuery(
        scope: CoroutineScope,
        query: String,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
    ) {
        updateState { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length < 2) {
            updateState { it.copy(searchResults = emptyList(), isSearchingPlacesLoading = false) }
            return
        }
        searchJob = scope.launch {
            delay(300)
            updateState { it.copy(isSearchingPlacesLoading = true) }
            val results = searchPlacesUseCase(query)
            updateState { it.copy(searchResults = results, isSearchingPlacesLoading = false) }
        }
    }

    fun cancel() {
        searchJob?.cancel()
        searchJob = null
    }
}
