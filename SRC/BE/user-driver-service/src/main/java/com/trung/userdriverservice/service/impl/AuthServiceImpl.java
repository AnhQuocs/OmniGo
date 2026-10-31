package com.trung.userdriverservice.service.impl;

import com.trung.userdriverservice.dto.request.ChangePasswordRequest;
import com.trung.userdriverservice.dto.request.LoginRequest;
import com.trung.userdriverservice.dto.request.ResetPasswordRequest;
import com.trung.userdriverservice.dto.response.ApiResponse;
import com.trung.userdriverservice.dto.response.LoginResponse;
import com.trung.userdriverservice.entity.User;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.InvalidCredentialsException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.mapper.UserMapper;
import com.trung.userdriverservice.repository.UserRepository;
import com.trung.userdriverservice.security.JwtTokenProvider;
import com.trung.userdriverservice.security.RefreshTokenService;
import com.trung.userdriverservice.security.UserPrincipal;
import com.trung.userdriverservice.service.AuthService;
import com.trung.userdriverservice.service.EmailService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) throws ResourceNotFoundException, InvalidCredentialsException {
        // 1. Kiểm tra trước nếu tài khoản đã bị khóa trong CSDL
        var userOpt = userRepository.findByPhoneNumber(request.getPhoneNumber());
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            if (Boolean.TRUE.equals(u.getIsLocked())) {
                String reasonMsg = (u.getLockedReason() != null && !u.getLockedReason().trim().isEmpty())
                        ? u.getLockedReason()
                        : "Vi phạm quy định hệ thống";
                throw new InvalidCredentialsException("Tài khoản của bạn đã bị khóa bởi Quản trị viên. Lý do: " + reasonMsg);
            }
            if (Boolean.TRUE.equals(u.getIsDeleted())) {
                throw new InvalidCredentialsException("Tài khoản của bạn đã bị xóa khỏi hệ thống.");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword())
            );
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            if (Boolean.TRUE.equals(user.getIsLocked())) {
                String reasonMsg = (user.getLockedReason() != null && !user.getLockedReason().trim().isEmpty())
                        ? user.getLockedReason()
                        : "Vi phạm quy định hệ thống";
                throw new InvalidCredentialsException("Tài khoản của bạn đã bị khóa bởi Quản trị viên. Lý do: " + reasonMsg);
            }

            // 3. Tạo các token
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = refreshTokenService.generateAndSaveRefreshToken(user.getPhoneNumber());
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userMapper.toUserResponse(user))
                    .build();

            return ApiResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Đăng nhập thành công")
                    .data(loginResponse)
                    .timestamp(LocalDateTime.now())
                    .error(null)
                    .build();
        } catch (org.springframework.security.authentication.LockedException | org.springframework.security.authentication.DisabledException ex) {
            String reasonMsg = userOpt.map(User::getLockedReason).filter(r -> r != null && !r.trim().isEmpty()).orElse("Vi phạm quy định hệ thống");
            throw new InvalidCredentialsException("Tài khoản của bạn đã bị khóa bởi Quản trị viên. Lý do: " + reasonMsg);
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Số điện thoại hoặc mật khẩu không hợp lệ.");
        }
    }

    @Override
    public ApiResponse<LoginResponse> refresh(String oldRefreshToken, String oldAccessToken) throws InvalidCredentialsException, ResourceNotFoundException {
        String phoneNumber = refreshTokenService.getPhoneNumberFromRefreshToken(oldRefreshToken);
        if (!refreshTokenService.validateRefreshToken(phoneNumber, oldRefreshToken)) {
            throw new InvalidCredentialsException("Refresh token không hợp lệ hoặc đã hết hạn.");
        }
        if (oldAccessToken != null) {
            blacklistAccessToken(oldAccessToken);
        }
        refreshTokenService.deleteRefreshToken(phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại với số điện thoại: " + phoneNumber));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.generateAndSaveRefreshToken(phoneNumber);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toUserResponse(user))
                .build();

        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Làm mới token thành công")
                .data(loginResponse)
                .timestamp(LocalDateTime.now())
                .error(null)
                .build();
    }

    @Override
    public ApiResponse<String> logout(String accessToken, String refreshToken) {
        String phoneNumber = jwtTokenProvider.getPhoneNumberFromToken(accessToken);

        blacklistAccessToken(accessToken);

        refreshTokenService.deleteRefreshToken(phoneNumber);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Đăng xuất thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) throws BadRequestException, ResourceNotFoundException {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp.");
        }

        String verifiedKey = "otp:verified:" + request.getVerificationToken().trim();
        String verifiedEmail = redisTemplate.opsForValue().get(verifiedKey);

        if (verifiedEmail == null || verifiedEmail.isBlank()) {
            throw new BadRequestException("Phiên xác thực đặt lại mật khẩu đã hết hạn hoặc không hợp lệ. Vui lòng gửi lại mã OTP.");
        }

        User user = userRepository.findByEmail(verifiedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + verifiedEmail));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BadRequestException("Tài khoản đã bị xóa khỏi hệ thống.");
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new BadRequestException("Tài khoản đã bị khóa bởi Quản trị viên.");
        }

        // Cập nhật mật khẩu mới đã mã hóa BCrypt
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Hủy bỏ verificationToken để chống replay
        redisTemplate.delete(verifiedKey);

        // Thu hồi refresh token cũ
        refreshTokenService.deleteRefreshToken(user.getPhoneNumber());

        // Gửi email cảnh báo bảo mật
        emailService.sendPasswordChangedAlert(user.getEmail(), user.getFullName(), LocalDateTime.now());
        log.info("Đặt lại mật khẩu thành công cho người dùng ID: {}, email: {}", user.getId(), user.getEmail());

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> changePassword(Long userId, ChangePasswordRequest request) throws BadRequestException, ResourceNotFoundException {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không chính xác.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu hiện tại.");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Thu hồi refresh token cũ
        refreshTokenService.deleteRefreshToken(user.getPhoneNumber());

        // Gửi email thông báo bảo mật
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendPasswordChangedAlert(user.getEmail(), user.getFullName(), LocalDateTime.now());
        }
        log.info("Đổi mật khẩu thành công cho người dùng ID: {}", user.getId());

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Đổi mật khẩu thành công.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void blacklistAccessToken(String token) {
        try {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long diff = expiration.getTime() - System.currentTimeMillis();

            if (diff > 0) {
                redisTemplate.opsForValue().set(
                        "jwt_blacklist:" + token,
                        "revoked",
                        diff,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            log.warn("Không thể blacklist access token: {}", e.getMessage());
        }
    }
}
