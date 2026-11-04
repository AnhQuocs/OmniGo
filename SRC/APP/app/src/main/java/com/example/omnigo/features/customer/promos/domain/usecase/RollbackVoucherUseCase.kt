package com.example.omnigo.features.customer.promos.domain.usecase

import com.example.omnigo.features.customer.promos.domain.repository.VoucherRepository
import javax.inject.Inject

class RollbackVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(code: String, orderId: String? = null): Result<Unit> {
        return voucherRepository.rollbackVoucher(code, orderId)
    }
}
