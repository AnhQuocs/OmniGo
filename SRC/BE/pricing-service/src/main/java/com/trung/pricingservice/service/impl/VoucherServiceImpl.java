package com.trung.pricingservice.service.impl;

import com.trung.pricingservice.dto.request.ApplyVoucherRequest;
import com.trung.pricingservice.dto.request.CreateVoucherRequest;
import com.trung.pricingservice.dto.response.ApplyVoucherResponse;
import com.trung.pricingservice.dto.response.VoucherResponse;
import com.trung.pricingservice.entity.Voucher;
import com.trung.pricingservice.entity.VoucherUsage;
import com.trung.pricingservice.exception.BadRequestException;
import com.trung.pricingservice.exception.ResourceNotFoundException;
import com.trung.pricingservice.mapper.VoucherMapper;
import com.trung.pricingservice.repository.VoucherRedisRepository;
import com.trung.pricingservice.repository.VoucherRepository;
import com.trung.pricingservice.repository.VoucherUsageRepository;
import com.trung.pricingservice.service.VoucherService;
import com.trung.pricingservice.util.enums.ApplicableService;
import com.trung.pricingservice.util.enums.DiscountType;
import com.trung.pricingservice.util.enums.VoucherStatus;
import com.trung.pricingservice.util.enums.VoucherUsageStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trung.pricingservice.dto.request.CommitVoucherRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherJpaRepository;
    private final VoucherRedisRepository voucherRedisRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final VoucherMapper voucherMapper;

    @Override
    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        String normalizedCode = request.getCode().trim().toUpperCase();

        if (voucherJpaRepository.existsByCode(normalizedCode) || voucherRedisRepository.findByCode(normalizedCode).isPresent()) {
            throw new BadRequestException("Mã voucher '" + normalizedCode + "' đã tồn tại trên hệ thống!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getExpireAt().isBefore(request.getStartAt())) {
            throw new BadRequestException("Thời gian hết hạn phải sau thời gian kích hoạt!");
        }

        if (request.getExpireAt().isBefore(now)) {
            throw new BadRequestException("Thời gian hết hạn phải lớn hơn thời điểm hiện tại!");
        }

        if (request.getDiscountType() == DiscountType.PERCENTAGE) {
            if (request.getDiscountValue() <= 0 || request.getDiscountValue() > 100) {
                throw new BadRequestException("Phần trăm giảm giá phải nằm trong khoảng từ 1% đến 100%!");
            }
            if (request.getMaxDiscountAmount() == null || request.getMaxDiscountAmount() <= 0) {
                throw new BadRequestException("Voucher giảm theo % bắt buộc phải thiết lập mức giảm tối đa (maxDiscountAmount)!");
            }
        }

        // 1. Lưu vĩnh viễn vào PostgreSQL (pricing_db) - Source of Truth
        Voucher voucher = voucherMapper.toEntity(request);
        Voucher savedVoucher = voucherJpaRepository.save(voucher);

        // 2. Nạp (Cache & Quota) vào Redis với TTL tự hủy
        voucherRedisRepository.saveVoucher(savedVoucher);

        log.info("Admin đã tạo thành công voucher: {} (Đã lưu DB pricing_db và nạp Redis)", normalizedCode);
        return voucherMapper.toResponse(savedVoucher);
    }

    @Override
    public List<VoucherResponse> getAllVouchers() {
        // Thử lấy từ Redis
        List<Voucher> redisList = voucherRedisRepository.findAll();

        if (redisList.isEmpty()) {
            // Nếu Redis trống (ví dụ sau restart), fallback tải từ DB và warm up lại Redis
            List<Voucher> dbList = voucherJpaRepository.findAll();
            for (Voucher v : dbList) {
                if (!v.isExpired()) {
                    voucherRedisRepository.saveVoucher(v);
                }
            }
            return dbList.stream().map(voucherMapper::toResponse).collect(Collectors.toList());
        }

        return redisList.stream().map(voucherMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public VoucherResponse getVoucherByCode(String code) {
        String normalized = code.trim().toUpperCase();
        Voucher voucher = voucherRedisRepository.findByCode(normalized)
                .or(() -> voucherJpaRepository.findByCode(normalized))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + code));
        return voucherMapper.toResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse toggleStatus(String code) {
        String normalized = code.trim().toUpperCase();
        Voucher voucher = voucherJpaRepository.findByCode(normalized)
                .or(() -> voucherRedisRepository.findByCode(normalized))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + code));

        if (voucher.getStatus() == VoucherStatus.ACTIVE) {
            voucher.setStatus(VoucherStatus.INACTIVE);
        } else {
            voucher.setStatus(VoucherStatus.ACTIVE);
        }
        voucher.setUpdatedAt(LocalDateTime.now());

        // Cập nhật cả DB và Redis
        Voucher updated = voucherJpaRepository.save(voucher);
        voucherRedisRepository.saveVoucher(updated);

        log.info("Đã cập nhật trạng thái voucher {} thành: {}", normalized, updated.getStatus());
        return voucherMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVoucher(String code) {
        String normalized = code.trim().toUpperCase();
        Voucher voucher = voucherJpaRepository.findByCode(normalized)
                .or(() -> voucherRedisRepository.findByCode(normalized))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + code));

        // Xóa cả DB và Redis
        voucherJpaRepository.delete(voucher);
        voucherRedisRepository.delete(normalized);
        log.info("Đã xóa vĩnh viễn voucher {} khỏi DB pricing_db và Redis", normalized);
    }

    @Override
    @Transactional
    public ApplyVoucherResponse applyVoucher(ApplyVoucherRequest request) {
        String code = request.getCode().trim().toUpperCase();
        double orderAmount = request.getOrderAmount();
        String serviceType = request.getServiceType();
        String userId = request.getUserId();

        // Đọc từ Redis để đạt tốc độ cao nhất
        Voucher voucher = voucherRedisRepository.findByCode(code)
                .or(() -> voucherJpaRepository.findByCode(code))
                .orElse(null);

        if (voucher == null) {
            return ApplyVoucherResponse.builder()
                    .valid(false)
                    .message("Mã voucher không tồn tại hoặc đã hết hạn!")
                    .originalAmount(orderAmount)
                    .discountAmount(0.0)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            return ApplyVoucherResponse.builder()
                    .valid(false)
                    .message("Mã voucher hiện đang bị tạm khóa!")
                    .originalAmount(orderAmount)
                    .discountAmount(0.0)
                    .finalAmount(orderAmount)
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
            return ApplyVoucherResponse.builder()
                    .valid(false)
                    .message("Mã voucher chưa đến thời gian kích hoạt!")
                    .originalAmount(orderAmount)
                    .discountAmount(0.0)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (voucher.isExpired()) {
            return ApplyVoucherResponse.builder()
                    .valid(false)
                    .message("Mã voucher đã hết hạn sử dụng!")
                    .originalAmount(orderAmount)
                    .discountAmount(0.0)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (orderAmount < voucher.getMinOrderAmount()) {
            return ApplyVoucherResponse.builder()
                    .valid(false)
                    .message(String.format("Đơn hàng chưa đạt giá trị tối thiểu %,.0f VNĐ để áp dụng mã này!", voucher.getMinOrderAmount()))
                    .originalAmount(orderAmount)
                    .discountAmount(0.0)
                    .finalAmount(orderAmount)
                    .build();
        }

        // Kiểm tra phạm vi dịch vụ
        if (voucher.getApplicableService() != ApplicableService.ALL) {
            boolean match = serviceType != null && voucher.getApplicableService().name().equalsIgnoreCase(serviceType);
            if (!match) {
                return ApplyVoucherResponse.builder()
                        .valid(false)
                        .message("Mã voucher không áp dụng cho loại dịch vụ này!")
                        .originalAmount(orderAmount)
                        .discountAmount(0.0)
                        .finalAmount(orderAmount)
                        .build();
            }
        }

        // Kiểm tra quota theo user trên Redis
        if (userId != null && !userId.isBlank()) {
            int userUsage = voucherRedisRepository.getUserUsageCount(code, userId);
            if (userUsage >= voucher.getUsageLimitPerUser()) {
                return ApplyVoucherResponse.builder()
                        .valid(false)
                        .message("Bạn đã sử dụng hết số lượt cho phép (" + voucher.getUsageLimitPerUser() + " lượt) của mã này!")
                        .originalAmount(orderAmount)
                        .discountAmount(0.0)
                        .finalAmount(orderAmount)
                        .build();
            }
        }

        // Kiểm tra tồn kho (nếu có giới hạn số lượng)
        if (voucher.getTotalUsageLimit() > 0) {
            Integer stock = voucherRedisRepository.getRemainingStock(code);
            if ((stock != null && stock == 0) || voucher.getUsedCount() >= voucher.getTotalUsageLimit()) {
                return ApplyVoucherResponse.builder()
                        .valid(false)
                        .message("Rất tiếc, mã voucher đã hết lượt sử dụng!")
                        .originalAmount(orderAmount)
                        .discountAmount(0.0)
                        .finalAmount(orderAmount)
                        .build();
            }
        }

        // Tính tiền giảm giá (CHỈ TÍNH TOÁN PREVIEW, CHƯA TRỪ STOCK, CHƯA GHI NHẬN LƯỢT DÙNG)
        double discountAmount = 0.0;
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = orderAmount * (voucher.getDiscountValue() / 100.0);
            if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount() > 0) {
                discountAmount = Math.min(discountAmount, voucher.getMaxDiscountAmount());
            }
        } else if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discountAmount = Math.min(voucher.getDiscountValue(), orderAmount);
        }

        double finalAmount = Math.max(0.0, orderAmount - discountAmount);

        return ApplyVoucherResponse.builder()
                .valid(true)
                .message("Áp dụng mã voucher thành công!")
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .voucher(voucherMapper.toResponse(voucher))
                .build();
    }

    @Override
    @Transactional
    public ApplyVoucherResponse commitVoucher(CommitVoucherRequest request) {
        String code = request.getCode().trim().toUpperCase();
        String userId = request.getUserId();
        String orderId = request.getOrderId();
        String serviceType = request.getServiceType();
        double orderAmount = request.getOriginalAmount();
        double discountAmount = request.getDiscountAmount();
        double finalAmount = request.getFinalAmount();

        Voucher voucher = voucherRedisRepository.findByCode(code)
                .or(() -> voucherJpaRepository.findByCode(code))
                .orElse(null);

        if (voucher == null || voucher.getStatus() != VoucherStatus.ACTIVE || voucher.isExpired()) {
            return ApplyVoucherResponse.builder()
                    .valid(false)
                    .message("Mã voucher không hợp lệ hoặc đã hết hạn khi xác nhận đơn hàng!")
                    .build();
        }

        // 1. Kiểm tra quota user
        if (userId != null && !userId.isBlank()) {
            int userUsage = voucherRedisRepository.getUserUsageCount(code, userId);
            if (userUsage >= voucher.getUsageLimitPerUser()) {
                return ApplyVoucherResponse.builder()
                        .valid(false)
                        .message("Bạn đã sử dụng hết số lượt cho phép của mã này!")
                        .build();
            }
        }

        // 2. Trừ slot nếu có giới hạn số lượng (Atomic Decrement)
        if (voucher.getTotalUsageLimit() > 0) {
            boolean stockReserved = voucherRedisRepository.decrementStock(code);
            if (!stockReserved) {
                return ApplyVoucherResponse.builder()
                        .valid(false)
                        .message("Rất tiếc, mã voucher đã hết lượt sử dụng!")
                        .build();
            }
        }

        LocalDateTime now = LocalDateTime.now();

        // 3. Ghi nhận lượt dùng của user trên Redis
        if (userId != null && !userId.isBlank()) {
            Duration ttl = Duration.between(now, voucher.getExpireAt());
            voucherRedisRepository.recordUserUsage(code, userId, ttl);
        }

        // 4. Cập nhật số lượt usedCount vào bảng vouchers
        try {
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherJpaRepository.save(voucher);
        } catch (Exception ex) {
            log.warn("Lỗi cập nhật usedCount vào DB cho voucher {}: {}", code, ex.getMessage());
        }

        // 5. Lưu lịch sử sử dụng chi tiết gắn với Mã đơn/Cuốc xe vào bảng voucher_usages
        if (userId != null && !userId.isBlank()) {
            try {
                VoucherUsage usage = VoucherUsage.builder()
                        .voucherCode(code)
                        .userId(userId)
                        .orderId(orderId)
                        .serviceType(serviceType)
                        .originalAmount(orderAmount)
                        .discountAmount(discountAmount)
                        .finalAmount(finalAmount)
                        .status(VoucherUsageStatus.USED)
                        .usedAt(now)
                        .build();
                voucherUsageRepository.save(usage);
                log.info("Đã ghi nhận sử dụng voucher {} cho User ID {} gắn với Đơn/Cuốc #{}", code, userId, orderId);
            } catch (Exception ex) {
                log.error("Lỗi lưu lịch sử voucher_usages: {}", ex.getMessage());
            }
        }

        return ApplyVoucherResponse.builder()
                .valid(true)
                .message("Đã xác nhận sử dụng mã voucher thành công!")
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .voucher(voucherMapper.toResponse(voucher))
                .build();
    }

    @Override
    public List<VoucherResponse> getApplicableVouchers(double orderAmount, String serviceType, String userId) {
        return getAllVouchers().stream()
                .filter(v -> v.getStatus() == VoucherStatus.ACTIVE)
                .filter(v -> orderAmount >= v.getMinOrderAmount())
                .filter(v -> {
                    if (v.getApplicableService() == ApplicableService.ALL) {
                        return true;
                    }
                    return serviceType != null && v.getApplicableService().name().equalsIgnoreCase(serviceType);
                })
                .filter(v -> {
                    if (userId == null || userId.isBlank()) {
                        return true;
                    }
                    int used = voucherRedisRepository.getUserUsageCount(v.getCode(), userId);
                    return used < v.getUsageLimitPerUser();
                })
                .filter(v -> v.getTotalUsageLimit() <= 0 || v.getRemainingCount() > 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void rollbackVoucherUsage(String code, String userId) {
        rollbackVoucherUsage(code, userId, null);
    }

    @Override
    @Transactional
    public void rollbackVoucherUsage(String code, String userId, String orderId) {
        Optional<VoucherUsage> usageOpt = Optional.empty();

        if (orderId != null && !orderId.isBlank()) {
            if (code != null && !code.isBlank()) {
                usageOpt = voucherUsageRepository.findByVoucherCodeAndOrderId(code.trim().toUpperCase(), orderId);
            } else {
                usageOpt = voucherUsageRepository.findFirstByOrderIdAndStatus(orderId, VoucherUsageStatus.USED);
                if (usageOpt.isEmpty()) {
                    usageOpt = voucherUsageRepository.findFirstByOrderIdOrderByUsedAtDesc(orderId);
                }
            }
        }

        if (usageOpt.isEmpty() && code != null && !code.isBlank() && userId != null && !userId.isBlank()) {
            usageOpt = voucherUsageRepository.findFirstByVoucherCodeAndUserIdAndStatusOrderByUsedAtDesc(
                    code.trim().toUpperCase(), userId, VoucherUsageStatus.USED);
        }

        String targetCode = (code != null && !code.isBlank())
                ? code.trim().toUpperCase()
                : (usageOpt.map(VoucherUsage::getVoucherCode).orElse(null));
        String targetUserId = (userId != null && !userId.isBlank())
                ? userId
                : (usageOpt.map(VoucherUsage::getUserId).orElse(null));

        if (targetCode == null || targetCode.isBlank()) {
            log.warn("Không tìm thấy thông tin voucher để rollback (code={}, userId={}, orderId={})", code, userId, orderId);
            return;
        }

        // Kiểm tra tránh hoàn lại 2 lần (idempotent)
        if (usageOpt.isPresent() && usageOpt.get().getStatus() == VoucherUsageStatus.REFUNDED) {
            log.info("Lượt dùng voucher {} cho orderId {} đã ở trạng thái REFUNDED từ trước. Bỏ qua.", targetCode, orderId);
            return;
        }

        // 1. Hoàn lại stock và quota trên Redis
        voucherRedisRepository.incrementStock(targetCode);
        if (targetUserId != null && !targetUserId.isBlank()) {
            voucherRedisRepository.revertUserUsage(targetCode, targetUserId);
        }

        // 2. Giảm usedCount trong bảng vouchers
        voucherJpaRepository.findByCode(targetCode).ifPresent(v -> {
            if (v.getUsedCount() > 0) {
                v.setUsedCount(v.getUsedCount() - 1);
                voucherJpaRepository.save(v);
            }
        });

        // 3. Cập nhật trạng thái REFUNDED trong bảng voucher_usages
        if (usageOpt.isPresent()) {
            VoucherUsage usage = usageOpt.get();
            usage.setStatus(VoucherUsageStatus.REFUNDED);
            usage.setRefundedAt(LocalDateTime.now());
            voucherUsageRepository.save(usage);
            log.info("Đã đánh dấu REFUNDED cho lượt dùng voucher {} của User {} (orderId: {})", targetCode, targetUserId, orderId);
        }

        log.info("Đã hoàn tất rollback voucher {} cho user {} (orderId: {})", targetCode, targetUserId, orderId);
    }

    @Override
    public List<VoucherUsage> getVoucherUsages(String code) {
        return voucherUsageRepository.findByVoucherCodeOrderByUsedAtDesc(code.trim().toUpperCase());
    }

    @Override
    public List<VoucherUsage> getUserVoucherUsages(String userId) {
        return voucherUsageRepository.findByUserIdOrderByUsedAtDesc(userId);
    }
}
