package com.trung.pricingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "Số tiền gốc không được để trống")
    @Positive(message = "Số tiền gốc phải lớn hơn 0")
    private Double originalAmount;

    private Double discountAmount;

    private Double finalAmount;
}
