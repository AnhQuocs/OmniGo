package com.example.omnigo.features.customer.activity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.usecase.GetCustomerBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val getCustomerBookingsUseCase: GetCustomerBookingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getCustomerBookingsUseCase()) {
                is GetCustomerBookingsResult.Success -> {
                    val ongoing = result.bookings.filter {
                        it.status == BookingStatus.PENDING ||
                        it.status == BookingStatus.ACCEPTED ||
                        it.status == BookingStatus.ARRIVED ||
                        it.status == BookingStatus.IN_PROGRESS
                    }
                    val history = result.bookings.filter {
                        it.status == BookingStatus.COMPLETED ||
                        it.status == BookingStatus.CANCELLED
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ongoingBookings = ongoing,
                            historyBookings = history
                        )
                    }
                }
                is GetCustomerBookingsResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}
