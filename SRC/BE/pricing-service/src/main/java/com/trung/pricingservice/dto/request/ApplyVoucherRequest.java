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
public class ApplyVoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotNull(message = "Số tiền đơn hàng không được để trống")
    @Positive(message = "Số tiền đơn hàng phải lớn hơn 0")
    private Double orderAmount;

    @NotBlank(message = "Loại dịch vụ không được để trống (RIDE_HAILING hoặc FOOD_DELIVERY)")
    private String serviceType;

    private String userId;

    private String orderId;
}
