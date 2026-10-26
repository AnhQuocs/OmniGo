package com.trung.pricingservice.mapper;

import com.trung.pricingservice.dto.request.CreateVoucherRequest;
import com.trung.pricingservice.dto.response.VoucherResponse;
import com.trung.pricingservice.entity.Voucher;
import com.trung.pricingservice.util.enums.VoucherStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VoucherMapper {

    public Voucher toEntity(CreateVoucherRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return Voucher.builder()
                .code(request.getCode().trim().toUpperCase())
                .title(request.getTitle())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount())
                .startAt(request.getStartAt())
                .expireAt(request.getExpireAt())
                .totalUsageLimit(request.getTotalUsageLimit() != null && request.getTotalUsageLimit() > 0 ? request.getTotalUsageLimit() : 0)
                .usedCount(0)
                .usageLimitPerUser(request.getUsageLimitPerUser())
                .applicableService(request.getApplicableService())
                .status(VoucherStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public VoucherResponse toResponse(Voucher voucher) {
        int remaining = voucher.getTotalUsageLimit() <= 0 ? -1 : Math.max(0, voucher.getTotalUsageLimit() - voucher.getUsedCount());

        VoucherStatus calculatedStatus = voucher.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if (voucher.isExpired()) {
            calculatedStatus = VoucherStatus.EXPIRED;
        } else if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
            calculatedStatus = VoucherStatus.UPCOMING;
        }

        return VoucherResponse.builder()
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderAmount(voucher.getMinOrderAmount())
                .startAt(voucher.getStartAt())
                .expireAt(voucher.getExpireAt())
                .totalUsageLimit(voucher.getTotalUsageLimit())
                .usedCount(voucher.getUsedCount())
                .remainingCount(remaining)
                .usageLimitPerUser(voucher.getUsageLimitPerUser())
                .applicableService(voucher.getApplicableService())
                .status(calculatedStatus)
                .createdAt(voucher.getCreatedAt())
                .build();
    }
}
