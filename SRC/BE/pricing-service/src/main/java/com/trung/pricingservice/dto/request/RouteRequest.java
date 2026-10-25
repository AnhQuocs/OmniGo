package com.trung.pricingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotNull(message = "Vĩ độ điểm đón không được để trống")
    private Double startLatitude;

    @NotNull(message = "Kinh độ điểm đón không được để trống")
    private Double startLongitude;

    @NotNull(message = "Vĩ độ điểm đến không được để trống")
    private Double endLatitude;

    @NotNull(message = "Kinh độ điểm đến không được để trống")
    private Double endLongitude;


    @Builder.Default
    private String vehicleType = "BIKE";

    // Hỗ trợ alias tên tham số viết tắt nếu client truyền startLat / startLng
    public void setStartLat(Double startLat) {
        if (this.startLatitude == null) this.startLatitude = startLat;
    }

    public void setStartLng(Double startLng) {
        if (this.startLongitude == null) this.startLongitude = startLng;
    }

    public void setEndLat(Double endLat) {
        if (this.endLatitude == null) this.endLatitude = endLat;
    }

    public void setEndLng(Double endLng) {
        if (this.endLongitude == null) this.endLongitude = endLng;
    }
}
