package com.example.omnigo.features.customer.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.features.driver.earnings.domain.usecase.DepositWalletUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.DriverWithdrawUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverTransactionsUseCase
import com.example.omnigo.features.driver.earnings.domain.usecase.GetDriverWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerWalletUiState(
    val balance: Double = 0.0,
    val transactions: List<DriverWalletTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val showDepositDialog: Boolean = false,
    val showWithdrawDialog: Boolean = false,
    val openPaymentUrl: String? = null,
    val toastMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomerWalletViewModel @Inject constructor(
    private val getWalletUseCase: GetDriverWalletUseCase,
    private val getTransactionsUseCase: GetDriverTransactionsUseCase,
    private val depositWalletUseCase: DepositWalletUseCase,
    private val withdrawUseCase: DriverWithdrawUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerWalletUiState())
    val uiState: StateFlow<CustomerWalletUiState> = _uiState.asStateFlow()

    init {
        loadWalletData()
    }

    fun refresh() {
        loadWalletData()
    }

    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val walletRes = getWalletUseCase()
            val txRes = getTransactionsUseCase()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    balance = walletRes.getOrNull()?.balance ?: it.balance,
                    transactions = txRes.getOrDefault(emptyList())
                )
            }
        }
    }

    fun setShowDepositDialog(show: Boolean) {
        _uiState.update { it.copy(showDepositDialog = show) }
    }

    fun setShowWithdrawDialog(show: Boolean) {
        _uiState.update { it.copy(showWithdrawDialog = show) }
    }

    fun deposit(amount: Double, method: String = "VNPAY") {
        if (amount < 10000.0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            depositWalletUseCase(amount = amount, method = method)
                .onSuccess { payUrl ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            showDepositDialog = false,
                            openPaymentUrl = payUrl
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            toastMessage = err.message ?: "Deposit failed"
                        )
                    }
                }
        }
    }

    fun withdraw(amount: Double) {
        if (amount < 10000.0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            withdrawUseCase(amount)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            showWithdrawDialog = false,
                            toastMessage = "OK"
                        )
                    }
                    loadWalletData()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            toastMessage = err.message ?: "Withdraw failed"
                        )
                    }
                }
        }
    }

    fun clearOpenPaymentUrl() {
        _uiState.update { it.copy(openPaymentUrl = null) }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
