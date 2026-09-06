package com.trung.fooddeliveryservice.dto.response;

import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse implements Serializable {
    private Long id;
    private Long ownerId;
    private String name;
    private String phone;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private RestaurantStatus status;
    private String openTime;
    private String closeTime;
    private Double rating;
    private List<MenuItemResponse> menuItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
