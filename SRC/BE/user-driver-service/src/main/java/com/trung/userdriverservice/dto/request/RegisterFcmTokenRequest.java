package com.trung.userdriverservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterFcmTokenRequest {

    @NotBlank(message = "FCM Token không được để trống")
    private String fcmToken;

    @Builder.Default
    private String deviceType = "ANDROID";

    private String deviceModel;
}

