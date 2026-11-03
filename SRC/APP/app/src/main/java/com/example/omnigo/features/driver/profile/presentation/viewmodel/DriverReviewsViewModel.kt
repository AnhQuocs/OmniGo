package com.example.omnigo.features.driver.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.customer.food.domain.usecase.GetDriverFoodReviewsUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetDriverBookingReviewsUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.profile.presentation.state.DriverReviewsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverReviewsViewModel @Inject constructor(
    private val getDriverProfileUseCase: GetDriverProfileUseCase,
    private val getDriverBookingReviewsUseCase: GetDriverBookingReviewsUseCase,
    private val getDriverFoodReviewsUseCase: GetDriverFoodReviewsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverReviewsUiState())
    val uiState: StateFlow<DriverReviewsUiState> = _uiState.asStateFlow()

    init {
        loadAllReviews()
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTabIndex = tabIndex, selectedStarFilter = 0) }
    }

    fun onStarFilterSelected(star: Int) {
        _uiState.update { it.copy(selectedStarFilter = star) }
    }

    fun loadAllReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val profileId = getDriverProfileUseCase(null).getOrNull()?.driverId
            val fallbackId = sessionManager.getUserId() ?: 0L
            val driverId = if (profileId != null && profileId > 0L) profileId else fallbackId

            if (driverId <= 0L) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val rideDeferred = async { getDriverBookingReviewsUseCase(driverId) }
            val foodDeferred = async { getDriverFoodReviewsUseCase(driverId) }

            val rideResult = rideDeferred.await()
            val foodResult = foodDeferred.await()

            _uiState.update { state ->
                var updated = state.copy(isLoading = false)
                rideResult.onSuccess { summary ->
                    updated = updated.copy(
                        rideTotalReviews = summary.totalReviews,
                        rideAverageRating = summary.averageRating,
                        rideReviews = summary.reviews
                    )
                }
                foodResult.onSuccess { summary ->
                    updated = updated.copy(
                        foodTotalReviews = summary.totalReviews,
                        foodAverageRating = summary.averageRating,
                        foodReviews = summary.reviews
                    )
                }
                updated
            }
        }
    }
}
