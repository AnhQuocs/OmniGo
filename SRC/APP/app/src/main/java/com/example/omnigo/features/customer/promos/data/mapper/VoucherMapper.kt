package com.example.omnigo.features.customer.promos.data.mapper

import com.example.omnigo.features.customer.promos.data.remote.dto.ApplyVoucherResponseDto
import com.example.omnigo.features.customer.promos.data.remote.dto.VoucherDto
import com.example.omnigo.features.customer.promos.data.remote.dto.VoucherUsageDto
import com.example.omnigo.features.customer.promos.domain.model.AppliedVoucherResult
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.model.VoucherDiscountType
import com.example.omnigo.features.customer.promos.domain.model.VoucherUsage

object VoucherMapper {

    fun toDomain(dto: VoucherDto): Voucher {
        val type = if (dto.discountType.equals("PERCENTAGE", ignoreCase = true)) {
            VoucherDiscountType.PERCENTAGE
        } else {
            VoucherDiscountType.FIXED_AMOUNT
        }
        return Voucher(
            id = dto.id ?: if (!dto.code.isNullOrBlank()) kotlin.math.abs(dto.code.hashCode().toLong()) else 0L,
            code = dto.code.orEmpty(),
            name = dto.name.orEmpty().ifBlank { dto.code.orEmpty() },
            description = dto.description.orEmpty(),
            discountType = type,
            discountValue = dto.discountValue ?: 0.0,
            maxDiscountAmount = dto.maxDiscountAmount,
            minOrderAmount = dto.minOrderAmount ?: 0.0,
            applicableService = dto.applicableService?.uppercase() ?: "ALL",
            usageLimit = dto.usageLimit,
            usedCount = dto.usedCount ?: 0,
            perUserLimit = dto.perUserLimit ?: 1,
            startDate = dto.startDate,
            endDate = dto.endDate,
            active = dto.active ?: true
        )
    }

    fun toAppliedDomain(dto: ApplyVoucherResponseDto, fallbackCode: String, fallbackOrderAmount: Double): AppliedVoucherResult {
        val discount = dto.discountAmount ?: 0.0
        val orig = dto.originalAmount ?: fallbackOrderAmount
        val finalAmt = dto.finalAmount ?: (orig - discount).coerceAtLeast(0.0)
        return AppliedVoucherResult(
            voucherCode = dto.voucherCode?.takeIf { it.isNotBlank() } ?: fallbackCode,
            voucherName = dto.voucherName.orEmpty().ifBlank { fallbackCode },
            originalAmount = orig,
            discountAmount = discount,
            finalAmount = finalAmt,
            message = dto.message
        )
    }

    fun toUsageDomain(dto: VoucherUsageDto): VoucherUsage {
        return VoucherUsage(
            id = dto.id ?: 0L,
            voucherId = dto.voucherId ?: 0L,
            voucherCode = dto.voucherCode.orEmpty(),
            userId = dto.userId.orEmpty(),
            orderId = dto.orderId,
            serviceType = dto.serviceType.orEmpty().ifBlank { "ALL" },
            discountAmount = dto.discountAmount ?: 0.0,
            usedAt = dto.usedAt.orEmpty()
        )
    }
}
