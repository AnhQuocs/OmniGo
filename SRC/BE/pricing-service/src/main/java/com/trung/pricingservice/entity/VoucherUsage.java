package com.trung.pricingservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trung.pricingservice.util.enums.VoucherUsageStatus;
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
@Table(name = "voucher_usages", indexes = {
        @Index(name = "idx_usage_code", columnList = "voucherCode"),
        @Index(name = "idx_usage_user", columnList = "userId"),
        @Index(name = "idx_usage_order", columnList = "orderId")
})
public class VoucherUsage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String voucherCode;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(length = 100)
    private String orderId;

    @Column(length = 30)
    private String serviceType;

    @Column(nullable = false)
    private double originalAmount;

    @Column(nullable = false)
    private double discountAmount;

    @Column(nullable = false)
    private double finalAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherUsageStatus status = VoucherUsageStatus.USED;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private LocalDateTime usedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime refundedAt;

    @PrePersist
    protected void onCreate() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }
}
