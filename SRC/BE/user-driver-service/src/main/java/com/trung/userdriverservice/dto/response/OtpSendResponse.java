package com.trung.userdriverservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpSendResponse {

    private String target;

    private int expiresInSeconds;
}

