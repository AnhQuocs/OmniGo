package com.trung.fooddeliveryservice.dto.response;

import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantNearbyResponse implements Serializable {
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
    private Integer reviewCount;
    private Double distanceKm;
}
