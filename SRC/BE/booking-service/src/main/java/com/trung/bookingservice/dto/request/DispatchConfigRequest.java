package com.trung.bookingservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DispatchConfigRequest {

    @NotNull(message = "Bán kính tối đa cuốc xe không được để trống")
    @Min(value = 1, message = "Bán kính tối thiểu là 1 km")
    @Max(value = 50, message = "Bán kính tối đa là 50 km")
    private Double maxRideDistanceKm;

    @NotNull(message = "Bán kính tối đa giao đồ ăn không được để trống")
    @Min(value = 1, message = "Bán kính tối thiểu là 1 km")
    @Max(value = 50, message = "Bán kính tối đa là 50 km")
    private Double maxFoodDeliveryDistanceKm;
}
