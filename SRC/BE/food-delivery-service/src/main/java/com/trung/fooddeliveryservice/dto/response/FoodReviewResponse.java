package com.trung.fooddeliveryservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodReviewResponse {

    private Long id;
    private Long orderId;
    private Long restaurantId;
    private Long customerId;
    private String customerName;
    private Long driverId;

    private Integer restaurantRating;
    private String restaurantComment;
    private List<String> restaurantImages;

    private Integer driverRating;
    private String driverComment;

    private String merchantReply;
    private LocalDateTime merchantRepliedAt;

    private LocalDateTime editableUntil;
    private Boolean isEdited;
    private Boolean canEdit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonProperty("images")
    public List<String> getImages() {
        return restaurantImages;
    }

    @JsonProperty("restaurantReply")
    public String getRestaurantReply() {
        return merchantReply;
    }

    @JsonProperty("restaurantReplyAt")
    public LocalDateTime getRestaurantReplyAt() {
        return merchantRepliedAt;
    }
}
