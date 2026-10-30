package com.example.omnigo.features.driver.earnings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.domain.usecase.GetDriverDailyReportUseCase
import com.example.omnigo.features.driver.earnings.domain.model.DriverWallet
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.features.driver.earnings.domain.usecase.CreateDriverDepositUrlUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.DriverWithdrawUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverTransactionsUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverWalletUseCase
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
    val isActionLoading: Boolean = false,
    val selectedPeriod: EarningsPeriod = EarningsPeriod.TODAY,
    val dailyReport: DriverDailyReport? = null,
    val wallet: DriverWallet? = null,
    val walletTransactions: List<DriverWalletTransaction> = emptyList(),
    val foodOrders: List<DriverDeliveryOrder> = emptyList(),
    val chartPoints: List<EarningsChartPoint> = emptyList(),
    val tripItems: List<EarningsTripItem> = emptyList(),
    val showCashoutSheet: Boolean = false,
    val showTopupSheet: Boolean = false,
    val showWalletHistory: Boolean = false,
    val walletHistoryInitialTab: Int = 0,
    val showQuestDetail: Boolean = false,
    val toastMessage: String? = null,
    val openPaymentUrl: String? = null
)

@HiltViewModel
class DriverEarningsViewModel @Inject constructor(
    private val getDriverDailyReportUseCase: GetDriverDailyReportUseCase,
    private val driverRepository: DriverRepository,
    private val driverWithdrawUseCase: DriverWithdrawUseCase,
    private val createDriverDepositUrlUseCase: CreateDriverDepositUrlUseCase,
    private val getDriverWalletUseCase: GetDriverWalletUseCase,
    private val getDriverTransactionsUseCase: GetDriverTransactionsUseCase
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

            // Fetch real wallet & transactions concurrently
            val walletResult = getDriverWalletUseCase()
            val transactionsResult = getDriverTransactionsUseCase()

            val wallet = walletResult.getOrNull() ?: _uiState.value.wallet
            val transactions = transactionsResult.getOrDefault(emptyList())

            when (val result = getDriverDailyReportUseCase(forceRefresh = forceRefresh, period = apiPeriod)) {
                is GetDriverDailyReportResult.Success -> {
                    val report = result.report
                    val points = EarningsDataHelper.generateChartPoints(_uiState.value.selectedPeriod, report, orders)
                    val trips = EarningsDataHelper.extractTripItems(report, orders)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dailyReport = report,
                            wallet = wallet,
                            walletTransactions = transactions,
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
                            wallet = wallet,
                            walletTransactions = transactions,
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

    fun setShowCashoutSheet(show: Boolean) {
        _uiState.update { it.copy(showCashoutSheet = show) }
    }

    fun setShowTopupSheet(show: Boolean) {
        _uiState.update { it.copy(showTopupSheet = show) }
    }

    fun setShowWalletHistory(show: Boolean, initialTab: Int = 0) {
        _uiState.update { it.copy(showWalletHistory = show, walletHistoryInitialTab = initialTab) }
    }

    fun setShowQuestDetail(show: Boolean) {
        _uiState.update { it.copy(showQuestDetail = show) }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun clearOpenPaymentUrl() {
        _uiState.update { it.copy(openPaymentUrl = null) }
    }

    fun confirmCashout(amount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            val result = driverWithdrawUseCase(amount)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        showCashoutSheet = false,
                        toastMessage = "Đã gửi yêu cầu rút tiền thành công!"
                    )
                }
                loadEarningsReport(forceRefresh = true)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        toastMessage = "Rút tiền thất bại: ${e.message ?: "Lỗi hệ thống"}"
                    )
                }
            }
        }
    }

    fun confirmTopup(amount: Double, method: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            val result = createDriverDepositUrlUseCase(method = method, amount = amount)
            result.onSuccess { url ->
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        showTopupSheet = false,
                        openPaymentUrl = url,
                        toastMessage = "Đang chuyển tiếp tới cổng thanh toán..."
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        toastMessage = "Khởi tạo thanh toán thất bại: ${e.message ?: "Lỗi kết nối"}"
                    )
                }
            }
        }
    }
}
