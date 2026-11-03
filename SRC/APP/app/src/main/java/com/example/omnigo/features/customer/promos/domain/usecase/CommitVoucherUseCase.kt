package com.example.omnigo.features.customer.promos.domain.usecase

import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.repository.VoucherRepository
import javax.inject.Inject

class CommitVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(
        code: String,
        orderAmount: Double,
        serviceType: String,
        orderId: String? = null
    ): Result<AppliedVoucherResult> {
        return voucherRepository.commitVoucher(code, orderAmount, serviceType, orderId)
    }
}
