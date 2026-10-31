package com.trung.userdriverservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpSendRequest {

    private String email;

    private String phoneNumber;

    @NotBlank(message = "Mục đích gửi OTP không được để trống")
    private String purpose;
}

