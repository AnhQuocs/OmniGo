package com.trung.bookingservice.controller;

import com.trung.bookingservice.dto.request.DispatchConfigRequest;
import com.trung.bookingservice.dto.response.ApiResponse;
import com.trung.bookingservice.service.DispatchConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings/admin/dispatch-config")
@RequiredArgsConstructor
public class AdminDispatchController {

    private final DispatchConfigService dispatchConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Double>>> getDispatchConfig() {
        Map<String, Double> config = dispatchConfigService.getDispatchConfig();
        return ResponseEntity.ok(ApiResponse.<Map<String, Double>>builder()
                .success(true)
                .message("Lấy cấu hình bán kính điều phối thành công")
                .data(config)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, Double>>> updateDispatchConfig(
            @Valid @RequestBody DispatchConfigRequest request) {
        Map<String, Double> updated = dispatchConfigService.updateDispatchConfig(request);
        return ResponseEntity.ok(ApiResponse.<Map<String, Double>>builder()
                .success(true)
                .message("Cập nhật cấu hình bán kính điều phối thành công!")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
