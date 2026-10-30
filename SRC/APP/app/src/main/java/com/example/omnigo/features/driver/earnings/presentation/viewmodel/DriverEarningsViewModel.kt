package com.example.omnigo.features.driver.earnings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
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
    val showCashoutDialog: Boolean = false,
    val showTopupDialog: Boolean = false
)

@HiltViewModel
class DriverEarningsViewModel @Inject constructor(
    private val getDriverDailyReportUseCase: GetDriverDailyReportUseCase
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getDriverDailyReportUseCase(forceRefresh)) {
                is GetDriverDailyReportResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, dailyReport = result.report)
                    }
                }
                is GetDriverDailyReportResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
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
