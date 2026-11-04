package com.example.omnigo.features.customer.promos.presentation.state

import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.model.VoucherUsage
import com.example.omnigo.utils.UiText

data class PromosUiState(
    val isLoading: Boolean = false,
    val selectedFilterIndex: Int = 0,
    val promoCodeInput: String = "",
    val vouchers: List<Voucher> = emptyList(),
    val allVouchersCount: Int = 0,
    val rideVouchersCount: Int = 0,
    val foodVouchersCount: Int = 0,
    val detailVoucher: Voucher? = null,
    val appliedPreview: AppliedVoucherResult? = null,
    val showHistorySheet: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val usageHistory: List<VoucherUsage> = emptyList(),
    val statusMessage: UiText? = null
)
