package com.example.omnigo.features.customer.promos.domain.usecase

import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.repository.VoucherRepository
import javax.inject.Inject

class ApplyVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(
        code: String,
        orderAmount: Double,
        serviceType: String
    ): Result<AppliedVoucherResult> {
        return voucherRepository.applyVoucher(code, orderAmount, serviceType)
    }
}
