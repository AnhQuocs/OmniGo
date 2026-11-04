package com.example.omnigo.features.customer.promos.domain.model

enum class VoucherDiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class Voucher(
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val discountType: VoucherDiscountType,
    val discountValue: Double,
    val maxDiscountAmount: Double?,
    val minOrderAmount: Double,
    val applicableService: String,
    val usageLimit: Int?,
    val usedCount: Int,
    val perUserLimit: Int,
    val startDate: String?,
    val endDate: String?,
    val active: Boolean
)

data class AppliedVoucherResult(
    val voucherCode: String,
    val voucherName: String,
    val originalAmount: Double,
    val discountAmount: Double,
    val finalAmount: Double,
    val message: String?
)

data class VoucherUsage(
    val id: Long,
    val voucherId: Long,
    val voucherCode: String,
    val userId: String,
    val orderId: String?,
    val serviceType: String,
    val discountAmount: Double,
    val usedAt: String
)
