package com.trung.userdriverservice.controller;
import com.trung.userdriverservice.dto.request.PageRequestDTO;
import com.trung.userdriverservice.dto.request.UserLockRequest;
import com.trung.userdriverservice.dto.request.UserRegisterRequest;
import com.trung.userdriverservice.dto.response.ApiResponse;
import com.trung.userdriverservice.dto.response.LoginResponse;
import com.trung.userdriverservice.dto.response.PageResponseDTO;
import com.trung.userdriverservice.dto.response.UserResponse;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.InvalidCredentialsException;
import com.trung.userdriverservice.exception.ResourceConflictException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.service.UserService;
import com.trung.userdriverservice.service.UserAvatarService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAvatarService userAvatarService;

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleAvatarUploadFailure(
            org.springframework.web.server.ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.builder()
                .success(false).message(ex.getReason()).timestamp(LocalDateTime.now()).build());
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DRIVER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam("file") MultipartFile file) throws BadRequestException, ResourceNotFoundException {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Cập nhật ảnh đại diện thành công")
                .data(userAvatarService.updateAvatar(currentUserId, file))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<LoginResponse>> registerCustomer(@Valid @RequestBody UserRegisterRequest request) throws ResourceConflictException, BadRequestException, InvalidCredentialsException {

        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Đăng ký tài khoản khách hàng thành công")
                .data(userService.registerCustomer(request).getData())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseDTO<UserResponse>> getAllUser(@ModelAttribute PageRequestDTO requestDTO) throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.getAllUsers(requestDTO));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId) throws ResourceNotFoundException, BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        return ResponseEntity.ok(userService.getUserById(currentUserId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (#currentUserId != null and #currentUserId == #id)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId) throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserLock(
            @PathVariable Long id,
            @Valid @RequestBody UserLockRequest request) throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.toggleUserLock(id, request));
    }
}
