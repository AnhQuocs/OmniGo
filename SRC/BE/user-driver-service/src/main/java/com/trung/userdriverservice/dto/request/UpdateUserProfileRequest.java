package com.trung.userdriverservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserProfileRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Email(message = "Địa chỉ email không đúng định dạng")
    private String email;
}

