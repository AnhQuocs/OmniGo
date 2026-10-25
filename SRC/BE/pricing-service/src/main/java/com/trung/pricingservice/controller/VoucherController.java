package com.trung.pricingservice.controller;

import com.trung.pricingservice.dto.request.ApplyVoucherRequest;
import com.trung.pricingservice.dto.response.ApiResponse;
import com.trung.pricingservice.dto.response.ApplyVoucherResponse;
import com.trung.pricingservice.dto.response.VoucherResponse;
import com.trung.pricingservice.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplyVoucherResponse>> applyVoucher(
            @Valid @RequestBody ApplyVoucherRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        if ((request.getUserId() == null || request.getUserId().isBlank()) && headerUserId != null) {
            request.setUserId(headerUserId);
        }

        ApplyVoucherResponse response = voucherService.applyVoucher(request);
        return ResponseEntity.ok(ApiResponse.<ApplyVoucherResponse>builder()
                .success(response.isValid())
                .message(response.getMessage())
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/commit")
    public ResponseEntity<ApiResponse<ApplyVoucherResponse>> commitVoucher(
            @Valid @RequestBody com.trung.pricingservice.dto.request.CommitVoucherRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        if ((request.getUserId() == null || request.getUserId().isBlank()) && headerUserId != null) {
            request.setUserId(headerUserId);
        }

        ApplyVoucherResponse response = voucherService.commitVoucher(request);
        return ResponseEntity.ok(ApiResponse.<ApplyVoucherResponse>builder()
                .success(response.isValid())
                .message(response.getMessage())
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/applicable")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getApplicableVouchers(
            @RequestParam(defaultValue = "0.0") double orderAmount,
            @RequestParam(defaultValue = "ALL") String serviceType,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(required = false) String userId) {

        String effectiveUserId = (userId != null && !userId.isBlank()) ? userId : headerUserId;
        List<VoucherResponse> vouchers = voucherService.getApplicableVouchers(orderAmount, serviceType, effectiveUserId);

        return ResponseEntity.ok(ApiResponse.<List<VoucherResponse>>builder()
                .success(true)
                .message("Lấy danh sách mã khuyến mãi khả dụng thành công")
                .data(vouchers)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/rollback")
    public ResponseEntity<ApiResponse<Void>> rollbackVoucher(
            @RequestParam(required = false) String code,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String orderId) {

        String effectiveUserId = (userId != null && !userId.isBlank()) ? userId : headerUserId;
        voucherService.rollbackVoucherUsage(code, effectiveUserId, orderId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã hoàn lại lượt sử dụng voucher thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/my-usages")
    public ResponseEntity<ApiResponse<List<com.trung.pricingservice.entity.VoucherUsage>>> getMyVoucherUsages(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(required = false) String userId) {

        String effectiveUserId = (userId != null && !userId.isBlank()) ? userId : headerUserId;
        if (effectiveUserId == null || effectiveUserId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.<List<com.trung.pricingservice.entity.VoucherUsage>>builder()
                    .success(true)
                    .message("User ID không hợp lệ")
                    .data(List.of())
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        List<com.trung.pricingservice.entity.VoucherUsage> usages = voucherService.getUserVoucherUsages(effectiveUserId);
        return ResponseEntity.ok(ApiResponse.<List<com.trung.pricingservice.entity.VoucherUsage>>builder()
                .success(true)
                .message("Lấy lịch sử dùng voucher của bạn thành công")
                .data(usages)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
