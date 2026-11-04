package com.trung.paymentservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Builder
public class PaymentUrlResponse {
    private String orderId;
    private String paymentUrl;
}