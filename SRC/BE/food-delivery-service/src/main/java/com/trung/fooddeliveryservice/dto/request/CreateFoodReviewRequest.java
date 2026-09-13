package com.trung.fooddeliveryservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFoodReviewRequest {

    @NotNull(message = "Order ID không được để trống")
    private Long orderId;

    @NotNull(message = "Số sao đánh giá quán không được để trống")
    @Min(value = 1, message = "Số sao quán tối thiểu là 1")
    @Max(value = 5, message = "Số sao quán tối đa là 5")
    private Integer restaurantRating;

    private String restaurantComment;

    private List<String> restaurantImages;
    private List<String> images;

    public List<String> getRestaurantImages() {
        if (restaurantImages != null && !restaurantImages.isEmpty()) {
            return restaurantImages;
        }
        return images;
    }

    @Min(value = 1, message = "Số sao tài xế tối thiểu là 1")
    @Max(value = 5, message = "Số sao tài xế tối đa là 5")
    private Integer driverRating;

    private String driverComment;
}
