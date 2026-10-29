package com.example.omnigo.features.driver.trips.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
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
    private val driverTripSessionManager: DriverTripSessionManager
) : ViewModel() {

    private var driverRepository: DriverRepository? = null

    private val _uiState = MutableStateFlow(DriverTripsUiState())
    val uiState: StateFlow<DriverTripsUiState> = _uiState.asStateFlow()

    init {
        loadDailyReport()
        observeActiveTrip()
    }

    @Inject
    fun injectDriverRepository(repository: DriverRepository) {
        this.driverRepository = repository
        loadFoodOrders()
    }

    private fun observeActiveTrip() {
        viewModelScope.launch {
            driverTripSessionManager.activeBooking.collect { activeBooking ->
                _uiState.update { it.copy(activeBooking = activeBooking) }
            }
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

    fun refresh() {
        loadDailyReport(forceRefresh = true)
        loadFoodOrders()
    }

    fun loadFoodOrders() {
        val repo = driverRepository ?: return
        viewModelScope.launch {
            repo.getDriverAllFoodOrders().onSuccess { allOrders ->
                val activeList = allOrders.filter { it.status.isActive }
                val historyList = allOrders.filter { !it.status.isActive }
                _uiState.update {
                    it.copy(
                        activeFoodOrders = activeList,
                        historyFoodOrders = historyList
                    )
                }
            }
        }
    }
}
