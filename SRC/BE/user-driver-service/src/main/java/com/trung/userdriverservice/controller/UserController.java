package com.trung.userdriverservice.controller;

import com.trung.userdriverservice.dto.request.PageRequestDTO;
import com.trung.userdriverservice.dto.request.RegisterFcmTokenRequest;
import com.trung.userdriverservice.dto.request.SaveAddressRequest;
import com.trung.userdriverservice.dto.request.UpdateUserProfileRequest;
import com.trung.userdriverservice.dto.request.UserLockRequest;
import com.trung.userdriverservice.dto.request.UserRegisterRequest;
import com.trung.userdriverservice.dto.response.ApiResponse;
import com.trung.userdriverservice.dto.response.LoginResponse;
import com.trung.userdriverservice.dto.response.PageResponseDTO;
import com.trung.userdriverservice.dto.response.SavedAddressResponse;
import com.trung.userdriverservice.dto.response.UserResponse;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.InvalidCredentialsException;
import com.trung.userdriverservice.exception.ResourceConflictException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.service.UserAvatarService;
import com.trung.userdriverservice.service.UserFcmTokenService;
import com.trung.userdriverservice.service.UserSavedAddressService;
import com.trung.userdriverservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAvatarService userAvatarService;
    private final UserFcmTokenService fcmTokenService;
    private final UserSavedAddressService savedAddressService;

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

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @Valid @RequestBody UpdateUserProfileRequest request) throws ResourceNotFoundException, ResourceConflictException, BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        return ResponseEntity.ok(userService.updateUserProfile(currentUserId, request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId) throws ResourceNotFoundException, BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        return ResponseEntity.ok(userService.deleteAccount(currentUserId));
    }

    // --- FCM TOKEN MANAGEMENT ---

    @PostMapping("/me/fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> registerFcmToken(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @Valid @RequestBody RegisterFcmTokenRequest request) throws BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        fcmTokenService.registerFcmToken(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Đăng ký FCM Token thành công")
                .data("OK")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/me/fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> removeFcmToken(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @RequestParam(required = false) String fcmToken) throws BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        fcmTokenService.removeFcmToken(currentUserId, fcmToken);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Đã hủy đăng ký FCM Token thành công")
                .data("OK")
                .timestamp(LocalDateTime.now())
                .build());
    }

    // --- SAVED ADDRESSES (SAVED PLACES) ---

    @GetMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SavedAddressResponse>>> getSavedAddresses(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId) throws BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        List<SavedAddressResponse> addresses = savedAddressService.getSavedAddresses(currentUserId);
        return ResponseEntity.ok(ApiResponse.<List<SavedAddressResponse>>builder()
                .success(true)
                .message("Lấy danh sách địa chỉ đã lưu thành công")
                .data(addresses)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SavedAddressResponse>> saveAddress(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @Valid @RequestBody SaveAddressRequest request) throws BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        SavedAddressResponse response = savedAddressService.saveAddress(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<SavedAddressResponse>builder()
                .success(true)
                .message("Lưu địa chỉ thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SavedAddressResponse>> updateAddress(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @PathVariable Long addressId,
            @Valid @RequestBody SaveAddressRequest request) throws BadRequestException, ResourceNotFoundException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        SavedAddressResponse response = savedAddressService.updateAddress(currentUserId, addressId, request);
        return ResponseEntity.ok(ApiResponse.<SavedAddressResponse>builder()
                .success(true)
                .message("Cập nhật địa chỉ thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @PathVariable Long addressId) throws BadRequestException, ResourceNotFoundException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        savedAddressService.deleteAddress(currentUserId, addressId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã xóa địa chỉ thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SavedAddressResponse>> setDefaultAddress(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @PathVariable Long addressId) throws BadRequestException, ResourceNotFoundException {
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id)");
        }
        SavedAddressResponse response = savedAddressService.setDefaultAddress(currentUserId, addressId);
        return ResponseEntity.ok(ApiResponse.<SavedAddressResponse>builder()
                .success(true)
                .message("Đã thiết lập địa chỉ mặc định thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
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
