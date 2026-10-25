package com.trung.pricingservice.repository;

import com.trung.pricingservice.entity.VoucherUsage;
import com.trung.pricingservice.util.enums.VoucherUsageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    List<VoucherUsage> findByVoucherCodeOrderByUsedAtDesc(String voucherCode);

    List<VoucherUsage> findByUserIdOrderByUsedAtDesc(String userId);

    Optional<VoucherUsage> findFirstByVoucherCodeAndUserIdAndStatusOrderByUsedAtDesc(
            String voucherCode, String userId, VoucherUsageStatus status);

    Optional<VoucherUsage> findByVoucherCodeAndOrderId(String voucherCode, String orderId);

    Optional<VoucherUsage> findFirstByOrderIdAndStatus(String orderId, VoucherUsageStatus status);

    Optional<VoucherUsage> findFirstByOrderIdOrderByUsedAtDesc(String orderId);

    long countByVoucherCodeAndUserIdAndStatus(String voucherCode, String userId, VoucherUsageStatus status);
}
