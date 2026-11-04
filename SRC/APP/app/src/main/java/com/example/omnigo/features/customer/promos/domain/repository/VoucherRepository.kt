package com.example.omnigo.features.customer.promos.domain.repository

import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.model.VoucherUsage

interface VoucherRepository {
    suspend fun getVouchers(serviceType: String? = null): Result<List<Voucher>>
    suspend fun getApplicableVouchers(orderAmount: Double, serviceType: String): Result<List<Voucher>>
    suspend fun applyVoucher(code: String, orderAmount: Double, serviceType: String): Result<AppliedVoucherResult>
    suspend fun commitVoucher(
        code: String,
        orderAmount: Double,
        serviceType: String,
        orderId: String? = null,
        discountAmount: Double = 0.0
    ): Result<AppliedVoucherResult>
    suspend fun rollbackVoucher(code: String, orderId: String? = null): Result<Unit>
    suspend fun getMyVoucherUsages(): Result<List<VoucherUsage>>
}
