package com.trung.userdriverservice.service.impl;

import com.trung.userdriverservice.dto.request.OtpSendRequest;
import com.trung.userdriverservice.dto.request.OtpVerifyRequest;
import com.trung.userdriverservice.dto.response.OtpSendResponse;
import com.trung.userdriverservice.dto.response.OtpVerifyResponse;
import com.trung.userdriverservice.entity.User;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.repository.UserRepository;
import com.trung.userdriverservice.service.EmailService;
import com.trung.userdriverservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public static final int OTP_TTL_SECONDS = 60;
    public static final int VERIFIED_TOKEN_TTL_SECONDS = 600; // 10 phút

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String VERIFIED_KEY_PREFIX = "otp:verified:";

    @Override
    public OtpSendResponse sendOtp(OtpSendRequest request) throws BadRequestException, ResourceNotFoundException {
        String targetEmail = resolveTargetEmail(request.getEmail(), request.getPhoneNumber(), request.getPurpose());

        String redisKey = buildOtpKey(request.getPurpose(), targetEmail);

        // Kiểm tra cooldown / Rate limit: nếu còn hạn 60s thì không cho gửi spam
        Long remainingTtl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (remainingTtl != null && remainingTtl > 0) {
            log.warn("Yêu cầu gửi OTP quá nhanh cho email {}, thời gian chờ còn lại {}s", targetEmail, remainingTtl);
            throw new BadRequestException("Mã OTP đã được gửi. Vui lòng kiểm tra hòm thư hoặc chờ " + remainingTtl + " giây trước khi yêu cầu lại.");
        }

        // Sinh mã OTP 6 chữ số
        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        // Lưu vào Redis với TTL đúng 60 giây
        redisTemplate.opsForValue().set(redisKey, otpCode, OTP_TTL_SECONDS, TimeUnit.SECONDS);
        log.info("Đã tạo OTP cho {} ({}), mã: {}, TTL: {}s", targetEmail, request.getPurpose(), otpCode, OTP_TTL_SECONDS);

        // Gửi qua Email
        emailService.sendOtpEmail(targetEmail, otpCode, OTP_TTL_SECONDS);

        return OtpSendResponse.builder()
                .target(maskEmail(targetEmail))
                .expiresInSeconds(OTP_TTL_SECONDS)
                .build();
    }

    @Override
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) throws BadRequestException {
        String targetEmail = resolveTargetEmailQuietly(request.getEmail(), request.getPhoneNumber());
        if (targetEmail == null || targetEmail.isBlank()) {
            throw new BadRequestException("Vui lòng cung cấp email hoặc số điện thoại để xác thực OTP.");
        }

        String redisKey = buildOtpKey(request.getPurpose(), targetEmail);
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            log.warn("Mã OTP cho email {} đã hết hạn hoặc không tồn tại", targetEmail);
            throw new BadRequestException("Mã OTP đã hết hạn sau 60 giây hoặc không tồn tại. Vui lòng lấy mã mới.");
        }

        if (!storedOtp.equals(request.getOtpCode().trim())) {
            log.warn("Mã OTP nhập không chính xác cho email {}. Đã nhập: {}, Kỳ vọng: {}", targetEmail, request.getOtpCode(), storedOtp);
            throw new BadRequestException("Mã xác thực OTP không chính xác.");
        }

        // Khớp mã thành công: xóa ngay mã OTP đã sử dụng
        redisTemplate.delete(redisKey);

        // Sinh verificationToken và lưu tạm thời vào Redis để tiến hành bước tiếp theo (đặt lại mật khẩu)
        String verificationToken = UUID.randomUUID().toString();
        String verifiedKey = VERIFIED_KEY_PREFIX + verificationToken;
        redisTemplate.opsForValue().set(verifiedKey, targetEmail, VERIFIED_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);

        log.info("Xác thực OTP thành công cho email {}, đã cấp token {}", targetEmail, verificationToken);

        return OtpVerifyResponse.builder()
                .verificationToken(verificationToken)
                .build();
    }

    private String resolveTargetEmail(String email, String phoneNumber, String purpose) throws BadRequestException, ResourceNotFoundException {
        if (email != null && !email.trim().isEmpty()) {
            return email.trim().toLowerCase();
        }

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            String normalizedPhone = phoneNumber.trim();
            Optional<User> userOpt = userRepository.findByPhoneNumber(normalizedPhone);
            if (userOpt.isEmpty()) {
                throw new ResourceNotFoundException("Không tìm thấy tài khoản tương ứng với số điện thoại: " + normalizedPhone);
            }
            User user = userOpt.get();
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new BadRequestException("Tài khoản chưa được liên kết email để nhận mã OTP.");
            }
            return user.getEmail().trim().toLowerCase();
        }

        throw new BadRequestException("Vui lòng cung cấp email hoặc số điện thoại.");
    }

    private String resolveTargetEmailQuietly(String email, String phoneNumber) {
        if (email != null && !email.trim().isEmpty()) {
            return email.trim().toLowerCase();
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber.trim());
            return userOpt.map(User::getEmail).orElse(null);
        }
        return null;
    }

    private String buildOtpKey(String purpose, String email) {
        String normalizedPurpose = (purpose != null) ? purpose.trim().toUpperCase() : "GENERAL";
        return OTP_KEY_PREFIX + normalizedPurpose + ":" + email.trim().toLowerCase();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }
}

