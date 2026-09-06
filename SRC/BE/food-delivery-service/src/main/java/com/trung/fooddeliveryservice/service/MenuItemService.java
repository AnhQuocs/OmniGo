package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.request.MenuItemRequest;
import com.trung.fooddeliveryservice.dto.response.MenuItemResponse;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;

import java.util.List;

public interface MenuItemService {

    MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request, Long ownerId) throws ResourceNotFoundException, UnauthorizedException;

    List<MenuItemResponse> getMenuItemsByRestaurantId(Long restaurantId);

    List<MenuItemResponse> getAvailableMenuItems(Long restaurantId);

    MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request, Long ownerId) throws ResourceNotFoundException, UnauthorizedException;

    void deleteMenuItem(Long itemId, Long ownerId) throws ResourceNotFoundException, UnauthorizedException;
}
