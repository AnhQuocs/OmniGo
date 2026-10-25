package com.trung.pricingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyVoucherResponse {
    private boolean valid;
    private String message;
    private double originalAmount;
    private double discountAmount;
    private double finalAmount;
    private VoucherResponse voucher;
}
