package com.trung.userdriverservice.service;

import java.time.LocalDateTime;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode, int expiresInSeconds);
    void sendPasswordChangedAlert(String toEmail, String fullName, LocalDateTime changeTime);
}

