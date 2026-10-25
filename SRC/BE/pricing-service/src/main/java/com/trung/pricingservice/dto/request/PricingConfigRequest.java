package com.trung.pricingservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfigRequest {

    @NotNull(message = "Giá mở cửa không được để trống")
    @Min(value = 0, message = "Giá mở cửa phải lớn hơn hoặc bằng 0")
    private Double baseFare;

    @NotNull(message = "Giá mỗi km không được để trống")
    @Min(value = 0, message = "Giá mỗi km phải lớn hơn hoặc bằng 0")
    private Double pricePerKm;

    @NotNull(message = "Giá mỗi phút không được để trống")
    @Min(value = 0, message = "Giá mỗi phút phải lớn hơn hoặc bằng 0")
    private Double pricePerMinute;

    // 2. Hệ số loại xe
    @Min(value = 0, message = "Hệ số xe máy phải >= 0")
    private Double bikeMultiplier;

    @Min(value = 0, message = "Hệ số ô tô 4 chỗ phải >= 0")
    private Double car4SeatMultiplier;

    @Min(value = 0, message = "Hệ số ô tô 7 chỗ phải >= 0")
    private Double car7SeatMultiplier;

    // 3. Phí giao đồ ăn
    @Min(value = 0, message = "Phí ship khởi điểm phải >= 0")
    private Double deliveryBaseFare;

    @Min(value = 0, message = "Khoảng cách khởi điểm phải >= 0")
    private Double deliveryBaseKm;

    @Min(value = 0, message = "Phí ship mỗi km tiếp theo phải >= 0")
    private Double deliveryPricePerKm;

    // 4. Google Routes & Fallback
    @Min(value = 1, message = "Hệ số uốn lượn fallback phải >= 1.0")
    private Double haversineFallbackMultiplier;

    @Min(value = 0, message = "Khoảng cách bypass phải >= 0")
    private Double minDistanceBypassMeters;

    // 5. Hệ số tăng giá cao điểm (Surge)
    @Min(value = 1, message = "Hệ số cao điểm nhẹ phải >= 1.0")
    private Double surgeLightMultiplier;

    @Min(value = 1, message = "Hệ số cao điểm vừa phải >= 1.0")
    private Double surgeModerateMultiplier;

    @Min(value = 1, message = "Hệ số cao điểm gắt phải >= 1.0")
    private Double surgeSevereMultiplier;
}