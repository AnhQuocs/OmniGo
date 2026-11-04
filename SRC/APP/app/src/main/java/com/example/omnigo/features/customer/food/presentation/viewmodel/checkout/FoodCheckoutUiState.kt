package com.example.omnigo.features.customer.food.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.utils.UiText

data class FoodCheckoutUiState(
    val isLoading: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val restaurant: Restaurant? = null,
    val restaurantId: Long? = null,
    val restaurantName: String = "",
    val items: List<CartItem> = emptyList(),
    val itemsSubtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val deliveryAddress: String = "",
    val deliveryLatitude: Double = 0.0,
    val deliveryLongitude: Double = 0.0,
    val driverNote: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val errorMessage: UiText? = null,
    val createdOrderId: Long? = null,
    val appliedVoucher: AppliedVoucherResult? = null,
    val applicableVouchers: List<Voucher> = emptyList(),
    val showVoucherSheet: Boolean = false,
    val isApplyingVoucher: Boolean = false,
    val openPaymentUrl: String? = null,
    val walletBalance: Double? = null,
    val isWalletLoading: Boolean = false,
    val showQuickTopUp: Boolean = false,
    val showWalletPaymentConfirm: Boolean = false,
    val isTopUpLoading: Boolean = false,
    val paymentWebViewUrl: String? = null
) {
    val discountAmount: Double
        get() = appliedVoucher?.discountAmount ?: 0.0

    val finalTotalAmount: Double
        get() = (totalAmount - discountAmount).coerceAtLeast(0.0)
}
