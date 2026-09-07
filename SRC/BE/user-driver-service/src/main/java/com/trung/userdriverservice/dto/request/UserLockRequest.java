package com.trung.userdriverservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLockRequest {

    @NotNull(message = "Trạng thái khóa không được để trống")
    private Boolean isLocked;

    private String reason;
}
