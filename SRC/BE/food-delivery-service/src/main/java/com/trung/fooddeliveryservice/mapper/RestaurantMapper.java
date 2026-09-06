package com.trung.fooddeliveryservice.mapper;

import com.trung.fooddeliveryservice.dto.request.RestaurantRequest;
import com.trung.fooddeliveryservice.dto.response.RestaurantResponse;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RestaurantMapper {

    private final MenuItemMapper menuItemMapper;

    public Restaurant toEntity(RestaurantRequest request, Long ownerId) {
        if (request == null) return null;
        return Restaurant.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(request.getImageUrl())
                .status(RestaurantStatus.OPEN)
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .rating(5.0)
                .build();
    }

    public RestaurantResponse toResponse(Restaurant entity) {
        if (entity == null) return null;
        return RestaurantResponse.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .imageUrl(entity.getImageUrl())
                .status(entity.getStatus())
                .openTime(entity.getOpenTime())
                .closeTime(entity.getCloseTime())
                .rating(entity.getRating())
                .menuItems(entity.getMenuItems() != null ? menuItemMapper.toResponseList(entity.getMenuItems()) : Collections.emptyList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<RestaurantResponse> toResponseList(List<Restaurant> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
