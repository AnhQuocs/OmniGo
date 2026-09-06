package com.trung.fooddeliveryservice.mapper;

import com.trung.fooddeliveryservice.dto.request.MenuItemRequest;
import com.trung.fooddeliveryservice.dto.response.MenuItemResponse;
import com.trung.fooddeliveryservice.entity.MenuItem;
import com.trung.fooddeliveryservice.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuItemMapper {

    public MenuItem toEntity(MenuItemRequest request, Restaurant restaurant) {
        if (request == null) return null;
        return MenuItem.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();
    }

    public MenuItemResponse toResponse(MenuItem entity) {
        if (entity == null) return null;
        return MenuItemResponse.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurant() != null ? entity.getRestaurant().getId() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .category(entity.getCategory())
                .isAvailable(entity.getIsAvailable())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<MenuItemResponse> toResponseList(List<MenuItem> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
