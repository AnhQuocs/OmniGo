package com.trung.fooddeliveryservice.controller;

import com.trung.fooddeliveryservice.dto.request.MenuItemRequest;
import com.trung.fooddeliveryservice.dto.response.ApiResponse;
import com.trung.fooddeliveryservice.dto.response.MenuItemResponse;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.service.MenuItemService;
import com.trung.fooddeliveryservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping("/restaurants/{restaurantId}/items")
    public ResponseEntity<ApiResponse<MenuItemResponse>> addMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        MenuItemResponse response = menuItemService.addMenuItem(restaurantId, request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Thêm món ăn thành công"));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @Valid @RequestBody MenuItemRequest request) throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        if (request.getRestaurantId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "restaurantId không được để trống khi tạo món");
        }
        MenuItemResponse response = menuItemService.addMenuItem(request.getRestaurantId(), request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Thêm món ăn thành công"));
    }

    @GetMapping("/restaurants/{restaurantId}/items")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItems(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "false") boolean availableOnly) {
        List<MenuItemResponse> list = availableOnly
                ? menuItemService.getAvailableMenuItems(restaurantId)
                : menuItemService.getMenuItemsByRestaurantId(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách món ăn thành công"));
    }

    @GetMapping("/items/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getItemsByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "false") boolean availableOnly) {
        List<MenuItemResponse> list = availableOnly
                ? menuItemService.getAvailableMenuItems(restaurantId)
                : menuItemService.getMenuItemsByRestaurantId(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách món ăn thành công"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        MenuItemResponse response = menuItemService.updateMenuItem(itemId, request, ownerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật món ăn thành công"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable Long itemId) throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        menuItemService.deleteMenuItem(itemId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa món ăn thành công"));
    }
}
