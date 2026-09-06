package com.trung.fooddeliveryservice.dto.request;

import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStatusRequest {

    @NotNull(message = "Trạng thái nhà hàng không được để trống")
    private RestaurantStatus status;
}
