package com.trung.userdriverservice.dto.request;

import com.trung.userdriverservice.util.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverApprovalRequest {

    @NotNull(message = "Trạng thái phê duyệt không được để trống")
    private ApprovalStatus status;

    private String reason;
}
