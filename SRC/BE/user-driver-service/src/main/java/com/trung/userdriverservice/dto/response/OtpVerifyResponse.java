package com.trung.userdriverservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerifyResponse {

    private String verificationToken;
}

