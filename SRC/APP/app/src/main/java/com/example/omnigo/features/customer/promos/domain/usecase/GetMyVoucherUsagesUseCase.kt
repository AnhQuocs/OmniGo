package com.example.omnigo.features.customer.promos.domain.usecase

import com.example.omnigo.features.customer.promos.domain.model.VoucherUsage
import com.example.omnigo.features.customer.promos.domain.repository.VoucherRepository
import javax.inject.Inject

class GetMyVoucherUsagesUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(): Result<List<VoucherUsage>> {
        return voucherRepository.getMyVoucherUsages()
    }
}
