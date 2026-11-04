package com.example.omnigo.features.customer.promos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.usecase.ApplyVoucherUseCase
import com.example.omnigo.features.customer.promos.domain.usecase.GetMyVoucherUsagesUseCase
import com.example.omnigo.features.customer.promos.domain.usecase.GetVouchersUseCase
import com.example.omnigo.features.customer.promos.presentation.state.PromosUiState
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.formatVnd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromosViewModel @Inject constructor(
    private val getVouchersUseCase: GetVouchersUseCase,
    private val applyVoucherUseCase: ApplyVoucherUseCase,
    private val getMyVoucherUsagesUseCase: GetMyVoucherUsagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromosUiState())
    val uiState: StateFlow<PromosUiState> = _uiState.asStateFlow()

    private var allCachedVouchers: List<Voucher> = emptyList()

    init {
        loadVouchers(filterIndex = 0)
    }

    fun selectFilter(index: Int) {
        if (_uiState.value.selectedFilterIndex == index) return
        _uiState.update { it.copy(selectedFilterIndex = index) }
        loadVouchers(index)
    }

    fun updatePromoCodeInput(code: String) {
        _uiState.update { it.copy(promoCodeInput = code.uppercase()) }
    }

    fun openDetail(voucher: Voucher) {
        _uiState.update { it.copy(detailVoucher = voucher) }
    }

    fun closeDetail() {
        _uiState.update { it.copy(detailVoucher = null) }
    }

    fun copyCode(code: String) {
        _uiState.update {
            it.copy(
                statusMessage = UiText.StringResource(
                    R.string.promos_copy_success,
                    code
                )
            )
        }
    }

    fun loadVouchers(filterIndex: Int = _uiState.value.selectedFilterIndex) {
        val serviceType = when (filterIndex) {
            1 -> "RIDE"
            2 -> "FOOD"
            else -> null
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getVouchersUseCase(serviceType)
                .onSuccess { list ->
                    if (filterIndex == 0) {
                        allCachedVouchers = list
                    }
                    val allCount = if (filterIndex == 0) list.size else allCachedVouchers.size
                    val rideCount = if (filterIndex == 0) {
                        list.count { it.applicableService.equals("RIDE", ignoreCase = true) || it.applicableService.equals("ALL", ignoreCase = true) }
                    } else _uiState.value.rideVouchersCount
                    val foodCount = if (filterIndex == 0) {
                        list.count { it.applicableService.equals("FOOD", ignoreCase = true) || it.applicableService.equals("ALL", ignoreCase = true) }
                    } else _uiState.value.foodVouchersCount

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            vouchers = list,
                            allVouchersCount = allCount,
                            rideVouchersCount = rideCount,
                            foodVouchersCount = foodCount
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = err.message?.let(UiText::DynamicString)
                        )
                    }
                }
        }
    }

    fun applyManualCode(currentOrderSubtotal: Double?, onSelectVoucher: ((String) -> Unit)?) {
        val code = _uiState.value.promoCodeInput.trim()
        if (code.isBlank()) return
        val serviceType = when (_uiState.value.selectedFilterIndex) {
            1 -> "RIDE"
            2 -> "FOOD"
            else -> "RIDE"
        }
        val orderAmt = currentOrderSubtotal ?: 100000.0
        viewModelScope.launch {
            applyVoucherUseCase(code = code, orderAmount = orderAmt, serviceType = serviceType)
                .onSuccess { res ->
                    onSelectVoucher?.invoke(res.voucherCode)
                    _uiState.update {
                        it.copy(
                            appliedPreview = res,
                            statusMessage = UiText.StringResource(
                                R.string.promos_saved_code_msg,
                                res.voucherCode,
                                res.discountAmount.formatVnd()
                            )
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(statusMessage = err.message?.let(UiText::DynamicString))
                    }
                }
        }
    }

    fun onUseVoucherCard(voucher: Voucher, currentOrderSubtotal: Double?, onSelectVoucher: ((String) -> Unit)?) {
        val service = if (voucher.applicableService == "FOOD") "FOOD" else "RIDE"
        val orderAmt = currentOrderSubtotal ?: voucher.minOrderAmount.coerceAtLeast(50000.0)
        viewModelScope.launch {
            applyVoucherUseCase(code = voucher.code, orderAmount = orderAmt, serviceType = service)
                .onSuccess { res ->
                    onSelectVoucher?.invoke(res.voucherCode)
                    _uiState.update {
                        it.copy(
                            appliedPreview = res,
                            statusMessage = UiText.StringResource(
                                R.string.promos_saved_code_msg,
                                res.voucherCode,
                                res.discountAmount.formatVnd()
                            )
                        )
                    }
                }
                .onFailure {
                    onSelectVoucher?.invoke(voucher.code)
                    _uiState.update {
                        it.copy(
                            statusMessage = UiText.StringResource(
                                R.string.promos_code_copied_ready,
                                voucher.code
                            )
                        )
                    }
                }
        }
    }

    fun openUsageHistory() {
        _uiState.update { it.copy(showHistorySheet = true, isLoadingHistory = true) }
        viewModelScope.launch {
            getMyVoucherUsagesUseCase()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoadingHistory = false, usageHistory = list) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingHistory = false,
                            statusMessage = err.message?.let(UiText::DynamicString)
                        )
                    }
                }
        }
    }

    fun closeUsageHistory() {
        _uiState.update { it.copy(showHistorySheet = false) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
