package com.trung.pricingservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trung.pricingservice.util.enums.ApplicableService;
import com.trung.pricingservice.util.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,20}$", message = "Mã voucher chỉ gồm chữ cái, số, gạch nối từ 3-20 ký tự")
    private String code;

    @NotBlank(message = "Tiêu đề voucher không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @Positive(message = "Giá trị giảm giá phải lớn hơn 0")
    private double discountValue;

    @Positive(message = "Mức giảm tối đa phải lớn hơn 0")
    private Double maxDiscountAmount;

    @PositiveOrZero(message = "Giá trị đơn hàng tối thiểu không được âm")
    @Builder.Default
    private double minOrderAmount = 0.0;

    @NotNull(message = "Thời gian kích hoạt không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    @NotNull(message = "Thời gian hết hạn không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expireAt;

    @Min(value = 0, message = "Tổng số lượng phát hành không được âm (0 hoặc để trống = Không giới hạn)")
    @Builder.Default
    private Integer totalUsageLimit = 0;

    @Min(value = 1, message = "Giới hạn mỗi khách hàng tối thiểu là 1")
    @Builder.Default
    private int usageLimitPerUser = 1;

    @Builder.Default
    private ApplicableService applicableService = ApplicableService.ALL;
}
