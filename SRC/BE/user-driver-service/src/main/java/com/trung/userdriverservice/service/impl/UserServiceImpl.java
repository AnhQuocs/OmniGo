package com.trung.userdriverservice.service.impl;

import com.trung.userdriverservice.dto.request.PageRequestDTO;
import com.trung.userdriverservice.dto.request.RegisterFcmTokenRequest;
import com.trung.userdriverservice.dto.request.UpdateUserProfileRequest;
import com.trung.userdriverservice.dto.request.UserLockRequest;
import com.trung.userdriverservice.dto.request.UserRegisterRequest;
import com.trung.userdriverservice.dto.response.ApiResponse;
import com.trung.userdriverservice.dto.response.LoginResponse;
import com.trung.userdriverservice.dto.response.PageResponseDTO;
import com.trung.userdriverservice.dto.response.UserResponse;
import com.trung.userdriverservice.entity.User;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.InvalidCredentialsException;
import com.trung.userdriverservice.exception.ResourceConflictException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.mapper.UserMapper;
import com.trung.userdriverservice.repository.UserFcmTokenRepository;
import com.trung.userdriverservice.repository.UserRepository;
import com.trung.userdriverservice.security.JwtTokenProvider;
import com.trung.userdriverservice.security.RefreshTokenService;
import com.trung.userdriverservice.service.FirebaseAuthService;
import com.trung.userdriverservice.service.UserFcmTokenService;
import com.trung.userdriverservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FirebaseAuthService firebaseAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final UserFcmTokenService fcmTokenService;

    @Override
    @Transactional
    public ApiResponse<LoginResponse> registerCustomer(UserRegisterRequest request) throws ResourceConflictException, BadRequestException, InvalidCredentialsException {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new BadRequestException("Vui lòng cung cấp số điện thoại để đăng ký.");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceConflictException("Số điện thoại đã được đăng ký.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email đã được đăng ký.");
        }

        User user = userMapper.toCustomerEntity(request);
        User savedUser = userRepository.save(user);

        // Tự động gán fcmToken nếu client gửi kèm trong request đăng ký
        if (request.getFcmToken() != null && !request.getFcmToken().trim().isEmpty()) {
            try {
                fcmTokenService.registerFcmToken(savedUser.getId(), RegisterFcmTokenRequest.builder()
                        .fcmToken(request.getFcmToken().trim())
                        .deviceType("ANDROID")
                        .build());
                log.info("Đã tự động gán FCM token khi đăng ký cho user ID: {}", savedUser.getId());
            } catch (Exception e) {
                log.warn("Không thể lưu FCM token khi đăng ký cho user ID {}: {}", savedUser.getId(), e.getMessage());
            }
        }

        // Sinh Access Token và Refresh Token cho người dùng vừa đăng ký
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
        String refreshToken = refreshTokenService.generateAndSaveRefreshToken(savedUser.getPhoneNumber());

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toUserResponse(savedUser))
                .build();

        return ApiResponse.<LoginResponse>builder()
                .data(loginResponse)
                .build();
    }

    @Override
    public PageResponseDTO<UserResponse> getAllUsers(PageRequestDTO pageRequestDTO) {
        int size = pageRequestDTO.getSize() != null ? pageRequestDTO.getSize() : 10;
        int page = pageRequestDTO.getPage() != null ? pageRequestDTO.getPage() : 0;
        String sortBy = pageRequestDTO.getSortBy() != null ? pageRequestDTO.getSortBy() : "id";
        Sort.Direction direction = pageRequestDTO.getSortDirection() != null ? Sort.Direction.fromString(pageRequestDTO.getSortDirection()) : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> userPage = userRepository.findAll(pageable);
        return PageResponseDTO.<UserResponse>builder()
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .content(userPage.map(userMapper::toUserResponse).getContent())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    public ApiResponse<UserResponse> getUserById(Long id) throws ResourceNotFoundException {
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Lấy thông tin người dùng thành công")
                .data(userMapper.toUserResponse(userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id))))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> toggleUserLock(Long userId, UserLockRequest request) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));

        boolean isLocked = Boolean.TRUE.equals(request.getIsLocked());
        user.setIsLocked(isLocked);
        if (isLocked) {
            user.setLockedReason(request.getReason() != null ? request.getReason().trim() : "Tài khoản bị khóa bởi Admin");
            user.setLockedAt(LocalDateTime.now());
            try {
                refreshTokenService.deleteRefreshToken(user.getPhoneNumber());
                redisTemplate.opsForValue().set("user_locked:" + userId, "true", Duration.ofDays(30));
            } catch (Exception ignored) {
            }
        } else {
            user.setLockedReason(null);
            user.setLockedAt(null);
            try {
                redisTemplate.delete("user_locked:" + userId);
            } catch (Exception ignored) {
            }
        }

        userRepository.save(user);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message(isLocked ? "Đã khóa tài khoản thành công" : "Đã mở khóa tài khoản thành công")
                .data(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    public void lockUserAccount(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));
        user.setIsLocked(true);
        user.setLockedReason("Tài khoản bị khóa bởi hệ thống");
        user.setLockedAt(LocalDateTime.now());
        try {
            refreshTokenService.deleteRefreshToken(user.getPhoneNumber());
            redisTemplate.opsForValue().set("user_locked:" + userId, "true", Duration.ofDays(30));
        } catch (Exception ignored) {
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateUserProfile(Long userId, UpdateUserProfileRequest request) throws ResourceNotFoundException, ResourceConflictException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new ResourceConflictException("Email " + newEmail + " đã được sử dụng bởi tài khoản khác.");
                }
                user.setEmail(newEmail);
            }
        }

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        User updatedUser = userRepository.save(user);
        log.info("Đã cập nhật thông tin hồ sơ cho User ID: {}", userId);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Cập nhật thông tin tài khoản thành công.")
                .data(userMapper.toUserResponse(updatedUser))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteAccount(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));

        user.setIsDeleted(true);
        userRepository.save(user);

        // Thu hồi token đăng nhập và thêm cờ khóa vào Redis
        try {
            refreshTokenService.deleteRefreshToken(user.getPhoneNumber());
            redisTemplate.opsForValue().set("user_locked:" + userId, "deleted", Duration.ofDays(30));
        } catch (Exception e) {
            log.warn("Lỗi khi xóa refresh token của user ID {}: {}", userId, e.getMessage());
        }

        // Xóa toàn bộ FCM token đã liên kết
        try {
            userFcmTokenRepository.deleteByUserId(userId);
            redisTemplate.delete("user:fcm:" + userId);
        } catch (Exception e) {
            log.warn("Lỗi khi xóa FCM tokens của user ID {}: {}", userId, e.getMessage());
        }

        log.info("Người dùng ID {} đã xóa tài khoản thành công (soft delete)", userId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Tài khoản của bạn đã được xóa thành công khỏi hệ thống.")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
