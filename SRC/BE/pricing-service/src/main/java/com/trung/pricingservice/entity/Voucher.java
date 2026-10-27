package com.trung.pricingservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trung.pricingservice.util.enums.ApplicableService;
import com.trung.pricingservice.util.enums.DiscountType;
import com.trung.pricingservice.util.enums.VoucherStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vouchers", indexes = {
        @Index(name = "idx_voucher_code", columnList = "code", unique = true)
})
public class Voucher implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscountType discountType;

    @Column(nullable = false)
    private double discountValue;

    private Double maxDiscountAmount;

    @Builder.Default
    @Column(nullable = false)
    private double minOrderAmount = 0.0;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime expireAt;

    @Builder.Default
    @Column(nullable = false)
    private int totalUsageLimit = 100;

    @Builder.Default
    @Column(nullable = false)
    private int usedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int usageLimitPerUser = 1;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicableService applicableService = ApplicableService.ALL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VoucherStatus status = VoucherStatus.ACTIVE;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expireAt != null && LocalDateTime.now().isAfter(expireAt);
    }

    public boolean isStarted() {
        return startAt == null || LocalDateTime.now().isAfter(startAt) || LocalDateTime.now().isEqual(startAt);
    }

    public boolean hasAvailableStock() {
        return totalUsageLimit <= 0 || usedCount < totalUsageLimit;
    }
}
