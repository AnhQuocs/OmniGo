package com.trung.userdriverservice.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirebaseAuthService {

    @Autowired(required = false)
    private FirebaseAuth firebaseAuth;

    public String verifyTokenAndExtractPhoneNumber(String firebaseToken) throws InvalidCredentialsException, BadRequestException {
        if (firebaseAuth == null) {
            throw new BadRequestException("Firebase Authentication chưa được cấu hình trên hệ thống.");
        }
        if (firebaseToken == null || firebaseToken.trim().isEmpty()) {
            throw new BadRequestException("Firebase ID Token không được để trống.");
        }

        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(firebaseToken.trim());
            String phoneNumber = (String) decodedToken.getClaims().get("phone_number");

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                throw new BadRequestException("Firebase ID Token không chứa thông tin số điện thoại hợp lệ.");
            }

            log.info("Xác thực Firebase Token thành công cho UID: {}, Phone từ Google: {}", decodedToken.getUid(), phoneNumber);
            return normalizePhoneNumber(phoneNumber);
        } catch (FirebaseAuthException e) {
            log.error("Lỗi xác thực Firebase ID Token: {}", e.getMessage());
            throw new InvalidCredentialsException("Firebase ID Token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            log.error("Lỗi không xác định khi verify Firebase Token: {}", e.getMessage(), e);
            throw new InvalidCredentialsException("Không thể xác thực Firebase ID Token: " + e.getMessage());
        }
    }


    private String normalizePhoneNumber(String rawPhone) {
        if (rawPhone == null) return null;
        String cleaned = rawPhone.replaceAll("[\\s\\-\\(\\)]", "");

        if (cleaned.startsWith("+84")) {
            return "0" + cleaned.substring(3);
        } else if (cleaned.startsWith("84") && cleaned.length() == 11) {
            return "0" + cleaned.substring(2);
        }
        return cleaned;
    }
}
