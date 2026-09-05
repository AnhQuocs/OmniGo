package com.trung.userdriverservice.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials:}")
    private String credentials;

    @Value("${firebase.credentials.base64:}")
    private String credentialsBase64;

    @Value("${firebase.credentials.json:}")
    private String credentialsJson;

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;

                // 1. Kiểm tra biến môi trường (hỗ trợ cả JSON trực tiếp hoặc Base64)
                String rawCreds = !credentials.trim().isEmpty() ? credentials.trim() :
                                  (!credentialsJson.trim().isEmpty() ? credentialsJson.trim() : credentialsBase64.trim());

                if (!rawCreds.isEmpty()) {
                    if (rawCreds.startsWith("{")) {
                        log.info("Khởi tạo Firebase từ chuỗi JSON môi trường...");
                        serviceAccount = new ByteArrayInputStream(rawCreds.getBytes(StandardCharsets.UTF_8));
                    } else {
                        log.info("Khởi tạo Firebase từ chuỗi Base64 môi trường...");
                        byte[] decoded = Base64.getDecoder().decode(rawCreds);
                        serviceAccount = new ByteArrayInputStream(decoded);
                    }
                } 
                // 2. Nếu không có biến môi trường -> Nạp từ file classpath local
                else {
                    ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                    if (resource.exists()) {
                        log.info("Khởi tạo Firebase từ file classpath local (firebase-service-account.json)...");
                        serviceAccount = resource.getInputStream();
                    } else {
                        log.warn("Không tìm thấy file firebase-service-account.json và không có biến môi trường credentials!");
                        return;
                    }
                }

                try (serviceAccount) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Khởi tạo Firebase Admin SDK thành công!");
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo Firebase Admin SDK: {}", e.getMessage(), e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
