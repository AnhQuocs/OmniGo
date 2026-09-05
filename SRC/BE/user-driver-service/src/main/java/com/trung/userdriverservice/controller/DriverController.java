package com.trung.userdriverservice.controller;

import com.trung.userdriverservice.dto.request.DriverAdminUpdateRequest;
import com.trung.userdriverservice.dto.request.DriverRegisterRequest;
import com.trung.userdriverservice.dto.request.DriverUpdateRequest;
import com.trung.userdriverservice.dto.response.ApiResponse;
import com.trung.userdriverservice.dto.response.DriverInternalResponse;
import com.trung.userdriverservice.dto.response.UserResponse;
import com.trung.userdriverservice.entity.DriverProfile;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.ResourceConflictException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.mapper.UserMapper;
import com.trung.userdriverservice.repository.DriverProfileRepository;
import com.trung.userdriverservice.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final DriverProfileRepository driverProfileRepository;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerDriver(@Valid @RequestBody DriverRegisterRequest request) throws ResourceConflictException {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Đăng ký tài khoản tài xế thành công")
                .data(driverService.registerDriver(request).getData())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<DriverInternalResponse>>> getAllDriversAdmin() {
        List<DriverProfile> list = driverProfileRepository.findAll();
        List<DriverInternalResponse> data = list.stream().map(userMapper::toDriverInternalResponse).toList();
        return ResponseEntity.ok(ApiResponse.<List<DriverInternalResponse>>builder()
                .success(true)
                .message("Lấy toàn bộ hồ sơ tài xế thành công")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/{driverId}/status")
    @PreAuthorize("hasRole('DRIVER') or (hasRole('DRIVER') and #currentDriverId == #driverId)")
    public ResponseEntity<ApiResponse<String>> toggleDriverActiveStatus(@PathVariable Long driverId,
                                                                        @RequestParam boolean isActive,
                                                                        @RequestHeader(name = "X-User-Id") Long currentDriverId) throws ResourceNotFoundException, BadRequestException {
        driverService.toggleDriverActiveStatus(driverId, isActive);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Cập nhật trạng thái tài xế thành công")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{driverId}/vehicle")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and #currentDriverId == #driverId)")
    public ResponseEntity<ApiResponse<UserResponse>> updateDriverVehicle(@PathVariable Long driverId,
                                                                         @Valid @RequestBody DriverUpdateRequest request,
                                                                         @RequestHeader(name = "X-User-Id", required = false) Long currentDriverId) throws ResourceNotFoundException, ResourceConflictException, BadRequestException {
        ApiResponse<UserResponse> response = driverService.updateDriverVehicle(driverId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{driverId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> adminUpdateDriver(@PathVariable Long driverId,
                                                                       @Valid @RequestBody DriverAdminUpdateRequest request) throws ResourceNotFoundException, ResourceConflictException, BadRequestException {
        ApiResponse<UserResponse> response = driverService.adminUpdateDriver(driverId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

