package com.trung.pricingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trung.pricingservice.util.enums.ApplicableService;
import com.trung.pricingservice.util.enums.DiscountType;
import com.trung.pricingservice.util.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    private String code;
    private String title;
    private String description;
    private DiscountType discountType;
    private double discountValue;
    private Double maxDiscountAmount;
    private double minOrderAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expireAt;

    private int totalUsageLimit;
    private int usedCount;
    private int remainingCount;
    private int usageLimitPerUser;
    private ApplicableService applicableService;
    private VoucherStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
