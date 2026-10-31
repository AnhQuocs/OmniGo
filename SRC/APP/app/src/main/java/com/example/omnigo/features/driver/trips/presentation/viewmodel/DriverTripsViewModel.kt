package com.example.omnigo.features.driver.trips.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.ride.domain.model.GetDriverBookingsResult
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.domain.usecase.GetDriverBookingsUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
import com.example.omnigo.features.driver.trips.domain.model.DriverTripFilter
import com.example.omnigo.features.driver.trips.domain.util.DriverTripsTimelineHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverTripsViewModel @Inject constructor(
    private val getDriverDailyReportUseCase: GetDriverDailyReportUseCase,
    private val getDriverBookingsUseCase: GetDriverBookingsUseCase,
    private val driverRepository: DriverRepository,
    private val driverTripSessionManager: DriverTripSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverTripsUiState())
    val uiState: StateFlow<DriverTripsUiState> = _uiState.asStateFlow()

    private var todayLabel: String = "Hôm nay"
    private var yesterdayLabel: String = "Hôm qua"

    init {
        loadDailyReport()
        observeActiveTrip()
        loadFoodOrders()
        loadDriverBookings()
    }

    private fun observeActiveTrip() {
        viewModelScope.launch {
            driverTripSessionManager.activeBooking.collect { activeBooking ->
                _uiState.update { it.copy(activeBooking = activeBooking) }
            }
        }
    }

    fun updateDateLabels(today: String, yesterday: String) {
        if (todayLabel == today && yesterdayLabel == yesterday) return
        todayLabel = today
        yesterdayLabel = yesterday
        recalculateTimeline()
    }

    fun onFilterSelected(filter: DriverTripFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        recalculateTimeline()
    }

    private fun recalculateTimeline() {
        _uiState.update { current ->
            current.copy(
                groupedHistory = DriverTripsTimelineHelper.groupTrips(
                    rides = current.historyBookings,
                    foods = current.historyFoodOrders,
                    filter = current.selectedFilter,
                    todayLabel = todayLabel,
                    yesterdayLabel = yesterdayLabel
                )
            )
        }
    }

    fun loadDailyReport(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReportLoading = true, reportErrorMessage = null) }
            when (val result = getDriverDailyReportUseCase(forceRefresh)) {
                is GetDriverDailyReportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isReportLoading = false,
                            dailyReport = result.report,
                            reportErrorMessage = null
                        )
                    }
                }
                is GetDriverDailyReportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isReportLoading = false,
                            reportErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadDriverBookings() {
        viewModelScope.launch {
            when (val result = getDriverBookingsUseCase()) {
                is GetDriverBookingsResult.Success -> {
                    _uiState.update { it.copy(historyBookings = result.bookings) }
                    recalculateTimeline()
                }
                is GetDriverBookingsResult.Error -> {
                    // Fail silently or keep existing data
                }
            }
        }
    }

    fun loadFoodOrders() {
        viewModelScope.launch {
            driverRepository.getDriverAllFoodOrders().onSuccess { allOrders ->
                val activeList = allOrders.filter { it.status.isActive }
                val historyList = allOrders.filter { !it.status.isActive }
                _uiState.update {
                    it.copy(
                        activeFoodOrders = activeList,
                        historyFoodOrders = historyList
                    )
                }
                recalculateTimeline()
            }
        }
    }

    fun refresh() {
        loadDailyReport(forceRefresh = true)
        loadFoodOrders()
        loadDriverBookings()
    }
}
