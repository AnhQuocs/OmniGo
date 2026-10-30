package com.example.omnigo.features.driver.earnings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.features.driver.earnings.domain.util.EarningsChartPoint
import com.example.omnigo.features.driver.earnings.domain.util.EarningsDataHelper
import com.example.omnigo.features.driver.earnings.domain.util.EarningsTripItem
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriverEarningsUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: EarningsPeriod = EarningsPeriod.TODAY,
    val dailyReport: DriverDailyReport? = null,
    val foodOrders: List<DriverDeliveryOrder> = emptyList(),
    val chartPoints: List<EarningsChartPoint> = emptyList(),
    val tripItems: List<EarningsTripItem> = emptyList(),
    val showCashoutDialog: Boolean = false,
    val showTopupDialog: Boolean = false
)

@HiltViewModel
class DriverEarningsViewModel @Inject constructor(
    private val getDriverDailyReportUseCase: GetDriverDailyReportUseCase,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverEarningsUiState())
    val uiState: StateFlow<DriverEarningsUiState> = _uiState.asStateFlow()

    init {
        loadEarningsReport()
    }

    fun onPeriodSelected(period: EarningsPeriod) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.update { it.copy(selectedPeriod = period) }
        loadEarningsReport(forceRefresh = true)
    }

    fun loadEarningsReport(forceRefresh: Boolean = false) {
        val apiPeriod = when (_uiState.value.selectedPeriod) {
            EarningsPeriod.TODAY -> "DAY"
            EarningsPeriod.THIS_WEEK -> "WEEK"
            EarningsPeriod.THIS_MONTH -> "MONTH"
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val orders = driverRepository.getDriverAllFoodOrders().getOrDefault(emptyList())
            when (val result = getDriverDailyReportUseCase(forceRefresh = forceRefresh, period = apiPeriod)) {
                is GetDriverDailyReportResult.Success -> {
                    val report = result.report
                    val points = EarningsDataHelper.generateChartPoints(_uiState.value.selectedPeriod, report, orders)
                    val trips = EarningsDataHelper.extractTripItems(report, orders)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dailyReport = report,
                            foodOrders = orders,
                            chartPoints = points,
                            tripItems = trips
                        )
                    }
                }
                is GetDriverDailyReportResult.Error -> {
                    val points = EarningsDataHelper.generateChartPoints(_uiState.value.selectedPeriod, null, orders)
                    val trips = EarningsDataHelper.extractTripItems(null, orders)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            foodOrders = orders,
                            chartPoints = points,
                            tripItems = trips
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadEarningsReport(forceRefresh = true)
    }

    fun setShowCashoutDialog(show: Boolean) {
        _uiState.update { it.copy(showCashoutDialog = show) }
    }

    fun setShowTopupDialog(show: Boolean) {
        _uiState.update { it.copy(showTopupDialog = show) }
    }
}
