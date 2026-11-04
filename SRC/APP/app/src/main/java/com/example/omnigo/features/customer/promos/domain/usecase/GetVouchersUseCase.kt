package com.example.omnigo.features.customer.promos.domain.usecase

import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.repository.VoucherRepository
import javax.inject.Inject

class GetVouchersUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(serviceType: String? = null): Result<List<Voucher>> {
        return voucherRepository.getVouchers(serviceType)
    }
}
