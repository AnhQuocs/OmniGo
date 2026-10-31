package com.trung.userdriverservice.service.impl;

import com.trung.userdriverservice.dto.request.RegisterFcmTokenRequest;
import com.trung.userdriverservice.entity.UserFcmToken;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.repository.UserFcmTokenRepository;
import com.trung.userdriverservice.service.UserFcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFcmTokenServiceImpl implements UserFcmTokenService {

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_FCM_SET_PREFIX = "user:fcm:";

    @Override
    @Transactional
    public void registerFcmToken(Long userId, RegisterFcmTokenRequest request) throws BadRequestException {
        if (request == null || request.getFcmToken() == null || request.getFcmToken().trim().isEmpty()) {
            throw new BadRequestException("FCM Token không được để trống.");
        }

        String token = request.getFcmToken().trim();
        Optional<UserFcmToken> existingOpt = userFcmTokenRepository.findByFcmToken(token);

        if (existingOpt.isPresent()) {
            UserFcmToken existing = existingOpt.get();
            // Nếu token đã được đăng ký trước đó cho user khác trên cùng thiết bị, xóa token khỏi Redis set của user cũ
            if (!existing.getUserId().equals(userId)) {
                redisTemplate.opsForSet().remove(REDIS_FCM_SET_PREFIX + existing.getUserId(), token);
            }
            existing.setUserId(userId);
            if (request.getDeviceType() != null && !request.getDeviceType().isBlank()) {
                existing.setDeviceType(request.getDeviceType().trim().toUpperCase());
            }
            if (request.getDeviceModel() != null && !request.getDeviceModel().isBlank()) {
                existing.setDeviceModel(request.getDeviceModel().trim());
            }
            existing.setLastActiveAt(LocalDateTime.now());
            userFcmTokenRepository.save(existing);
            log.info("Đã cập nhật chủ sở hữu FCM token cho User ID: {}", userId);
        } else {
            UserFcmToken newToken = UserFcmToken.builder()
                    .userId(userId)
                    .fcmToken(token)
                    .deviceType(request.getDeviceType() != null ? request.getDeviceType().trim().toUpperCase() : "ANDROID")
                    .deviceModel(request.getDeviceModel() != null ? request.getDeviceModel().trim() : null)
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            userFcmTokenRepository.save(newToken);
            log.info("Đã đăng ký mới FCM token cho User ID: {}", userId);
        }

        // Đồng bộ vào Redis set để các service khác (booking, food) có thể tra cứu token nhanh để gửi thông báo
        redisTemplate.opsForSet().add(REDIS_FCM_SET_PREFIX + userId, token);
    }

    @Override
    @Transactional
    public void removeFcmToken(Long userId, String fcmToken) {
        if (fcmToken != null && !fcmToken.trim().isEmpty()) {
            String token = fcmToken.trim();
            userFcmTokenRepository.deleteByUserIdAndFcmToken(userId, token);
            redisTemplate.opsForSet().remove(REDIS_FCM_SET_PREFIX + userId, token);
            log.info("Đã hủy đăng ký FCM token {} của User ID: {}", token, userId);
        } else {
            userFcmTokenRepository.deleteByUserId(userId);
            redisTemplate.delete(REDIS_FCM_SET_PREFIX + userId);
            log.info("Đã xóa toàn bộ FCM tokens của User ID: {}", userId);
        }
    }
}

