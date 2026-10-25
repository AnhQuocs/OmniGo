package com.trung.pricingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitVoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotBlank(message = "User ID không được để trống")
    private String userId;

    private String orderId;

    private String serviceType;

    private double originalAmount;

    private double discountAmount;

    private double finalAmount;
}
