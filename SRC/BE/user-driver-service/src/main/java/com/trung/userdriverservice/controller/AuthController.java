package com.trung.userdriverservice.controller;

import com.trung.userdriverservice.dto.request.ChangePasswordRequest;
import com.trung.userdriverservice.dto.request.LoginRequest;
import com.trung.userdriverservice.dto.request.OtpSendRequest;
import com.trung.userdriverservice.dto.request.OtpVerifyRequest;
import com.trung.userdriverservice.dto.request.ResetPasswordRequest;
import com.trung.userdriverservice.dto.response.ApiResponse;
import com.trung.userdriverservice.dto.response.LoginResponse;
import com.trung.userdriverservice.dto.response.OtpSendResponse;
import com.trung.userdriverservice.dto.response.OtpVerifyResponse;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.InvalidCredentialsException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.service.AuthService;
import com.trung.userdriverservice.service.OtpService;
import com.trung.userdriverservice.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenDurationMs;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) throws InvalidCredentialsException, ResourceNotFoundException {
        ApiResponse<LoginResponse> apiResponse = authService.login(request);

        String refreshToken = apiResponse.getData().getRefreshToken();

        setRefreshTokenCookie(response, refreshToken, refreshTokenDurationMs);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) throws InvalidCredentialsException, ResourceNotFoundException {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<LoginResponse>builder().success(false).message("Thiếu Refresh Token").build());
        }

        String authHeader = request.getHeader("Authorization");
        String accessToken = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;

        ApiResponse<LoginResponse> apiResponse = authService.refresh(refreshToken, accessToken);

        String newRefreshToken = apiResponse.getData().getRefreshToken();

        setRefreshTokenCookie(response, newRefreshToken, refreshTokenDurationMs);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            HttpServletResponse response) {

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        ApiResponse<String> apiResponse = authService.logout(accessToken, refreshToken);

        setRefreshTokenCookie(response, "", 0);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<OtpSendResponse>> sendOtp(@Valid @RequestBody OtpSendRequest request)
            throws BadRequestException, ResourceNotFoundException {
        OtpSendResponse sendResponse = otpService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.<OtpSendResponse>builder()
                .success(true)
                .message("Mã xác thực OTP đã được gửi đến email và có hiệu lực trong " + sendResponse.getExpiresInSeconds() + " giây.")
                .data(sendResponse)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<OtpVerifyResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request)
            throws BadRequestException {
        OtpVerifyResponse verifyResponse = otpService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.<OtpVerifyResponse>builder()
                .success(true)
                .message("Xác thực mã OTP thành công.")
                .data(verifyResponse)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
            throws BadRequestException, ResourceNotFoundException {
        ApiResponse<Void> response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest servletRequest,
            @Valid @RequestBody ChangePasswordRequest request)
            throws BadRequestException, ResourceNotFoundException {
        if (currentUserId == null && servletRequest != null) {
            String authHeader = servletRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    Object uidObj = claims.get("userId");
                    if (uidObj != null) {
                        currentUserId = Long.valueOf(uidObj.toString());
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (currentUserId == null) {
            throw new BadRequestException("Không tìm thấy thông tin định danh người dùng (X-User-Id). Vui lòng đăng nhập lại.");
        }
        ApiResponse<Void> response = authService.changePassword(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeMs) {
        long maxAgeSeconds = maxAgeMs / 1000;
        boolean isClearToken = (token == null || token.isEmpty());
        boolean isProduction = false;

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refresh_token", token != null ? token : "")
                .httpOnly(true)
                .path("/")
                .maxAge(isClearToken ? 0 : maxAgeSeconds);

        if (isProduction) {
            cookieBuilder.secure(true)
                    .sameSite("None")
                    .domain("ridehailingsystem.online");
        } else {
            cookieBuilder.secure(false)
                    .sameSite("Lax");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());

        if (isClearToken) {
            // Also clear legacy camelCase refreshToken cookie variant
            ResponseCookie clearCamelCase = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, clearCamelCase.toString());
        }
    }
}
