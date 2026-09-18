package com.example.omnigo.features.customer.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isPopularPlacesLoading: Boolean = false,
    val popularPlaces: List<Restaurant> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchPopularPlaces()
    }

    fun refresh() {
        fetchPopularPlaces()
    }

    private fun fetchPopularPlaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPopularPlacesLoading = true) }
            val result = getRestaurantsUseCase(search = null)
            when (result) {
                is GetRestaurantsResult.Success -> {
                    // Lấy ra 3 quán ăn đầu tiên làm "Nổi bật"
                    val topPlaces = result.restaurants.take(3)
                    _uiState.update { 
                        it.copy(isPopularPlacesLoading = false, popularPlaces = topPlaces) 
                    }
                }
                is GetRestaurantsResult.Error -> {
                    _uiState.update { 
                        it.copy(isPopularPlacesLoading = false) 
                    }
                }
            }
        }
    }
}
