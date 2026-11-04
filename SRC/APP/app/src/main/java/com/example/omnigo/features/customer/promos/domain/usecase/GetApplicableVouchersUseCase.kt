package com.example.omnigo.features.customer.promos.domain.usecase

import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.repository.VoucherRepository
import javax.inject.Inject

class GetApplicableVouchersUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(orderAmount: Double, serviceType: String): Result<List<Voucher>> {
        return voucherRepository.getApplicableVouchers(orderAmount, serviceType)
    }
}
