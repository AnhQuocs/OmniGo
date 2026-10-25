package com.trung.pricingservice.controller;

import com.trung.pricingservice.dto.request.CreateVoucherRequest;
import com.trung.pricingservice.dto.response.ApiResponse;
import com.trung.pricingservice.dto.response.VoucherResponse;
import com.trung.pricingservice.service.VoucherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import com.trung.pricingservice.entity.VoucherUsage;

@RestController
@RequestMapping("/api/v1/pricing/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse response = voucherService.createVoucher(request);
        return new ResponseEntity<>(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .message("Tạo mã khuyến mãi thành công vào Redis!")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build(), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAllVouchers() {
        List<VoucherResponse> list = voucherService.getAllVouchers();
        return ResponseEntity.ok(ApiResponse.<List<VoucherResponse>>builder()
                .success(true)
                .message("Lấy danh sách mã khuyến mãi thành công")
                .data(list)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherByCode(@PathVariable String code) {
        VoucherResponse voucher = voucherService.getVoucherByCode(code);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .message("Lấy chi tiết mã khuyến mãi thành công")
                .data(voucher)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/{code}/toggle")
    public ResponseEntity<ApiResponse<VoucherResponse>> toggleStatus(@PathVariable String code) {
        VoucherResponse updated = voucherService.toggleStatus(code);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái voucher thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable String code) {
        voucherService.deleteVoucher(code);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã xóa mã khuyến mãi khỏi Redis thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{code}/usages")
    public ResponseEntity<ApiResponse<List<VoucherUsage>>> getVoucherUsages(@PathVariable String code) {
        List<VoucherUsage> usages = voucherService.getVoucherUsages(code);
        return ResponseEntity.ok(ApiResponse.<List<VoucherUsage>>builder()
                .success(true)
                .message("Lấy lịch sử sử dụng voucher thành công")
                .data(usages)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
