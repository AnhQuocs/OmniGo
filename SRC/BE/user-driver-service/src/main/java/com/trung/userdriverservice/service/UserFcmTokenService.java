package com.trung.userdriverservice.service;

import com.trung.userdriverservice.dto.request.RegisterFcmTokenRequest;
import com.trung.userdriverservice.exception.BadRequestException;

public interface UserFcmTokenService {
    void registerFcmToken(Long userId, RegisterFcmTokenRequest request) throws BadRequestException;
    void removeFcmToken(Long userId, String fcmToken);
}

