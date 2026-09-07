package com.trung.userdriverservice.dto.response;

import com.trung.userdriverservice.util.enums.ApprovalStatus;
import com.trung.userdriverservice.util.enums.DriverStatus;
import com.trung.userdriverservice.util.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String phoneNumber;
    private String email;
    private String fullName;
    private Role role;
    private Boolean isLocked;
    private String lockedReason;
    private LocalDateTime lockedAt;
    private DriverStatus status;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private String vehicleType;
    private String licensePlate;
    private String vehicleModel;
    private LocalDateTime createdAt;
}
