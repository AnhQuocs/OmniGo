package com.trung.fooddeliveryservice.controller;

import com.trung.fooddeliveryservice.dto.request.RestaurantPartnerCreateRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantStatusRequest;
import com.trung.fooddeliveryservice.dto.response.ApiResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.service.RestaurantService;
import com.trung.fooddeliveryservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody RestaurantRequest request) throws BadRequestException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        RestaurantResponse response = restaurantService.createRestaurant(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đăng ký nhà hàng thành công"));
    }

    @PostMapping("/partner")
    public ResponseEntity<ApiResponse<RestaurantResponse>> registerPartner(
            @Valid @RequestBody RestaurantPartnerCreateRequest request) throws BadRequestException {
        RestaurantResponse response = restaurantService.registerPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đăng ký đối tác nhà hàng và tạo tài khoản thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getRestaurants(
            @RequestParam(required = false) String search) {
        List<RestaurantResponse> list;
        if (StringUtils.hasText(search)) {
            list = restaurantService.searchRestaurants(search);
        } else {
            list = restaurantService.getAllOpenRestaurants();
        }
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách nhà hàng thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(
            @PathVariable Long id) throws ResourceNotFoundException {
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin nhà hàng thành công"));
    }

    @GetMapping("/my-restaurant")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getMyRestaurant()
            throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        RestaurantResponse response = restaurantService.getRestaurantByOwnerId(ownerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin nhà hàng của bạn thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        RestaurantResponse response = restaurantService.updateRestaurant(id, request, ownerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật nhà hàng thành công"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RestaurantResponse>> toggleStatus(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantStatusRequest request) throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        RestaurantResponse response = restaurantService.toggleRestaurantStatus(id, ownerId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái nhà hàng thành công"));
    }
}
