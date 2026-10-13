package com.trung.fooddeliveryservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.trung.fooddeliveryservice.service.CloudinaryService;

import com.trung.fooddeliveryservice.dto.request.RestaurantLockRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantPartnerCreateRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantStatusRequest;
import com.trung.fooddeliveryservice.dto.response.ApiResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantNearbyResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.service.RestaurantService;
import com.trung.fooddeliveryservice.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) throws BadRequestException {
        String url = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url != null ? url : ""), "Tải ảnh lên thành công"));
    }

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

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<Page<RestaurantNearbyResponse>>> getNearbyRestaurants(
            @RequestParam(name = "latitude", required = false) String latStr,
            @RequestParam(name = "longitude", required = false) String lngStr,
            @RequestParam(name = "radiusKm", required = false) String radiusStr,
            @RequestParam(name = "page", required = false) String pageStr,
            @RequestParam(name = "limit", required = false) String limitStr,
            @RequestParam(name = "size", required = false) String sizeStr) throws BadRequestException {
        String effectiveLimit = StringUtils.hasText(limitStr) ? limitStr : sizeStr;
        Page<RestaurantNearbyResponse> page = restaurantService.getNearbyRestaurants(
                latStr, lngStr, radiusStr, pageStr, effectiveLimit);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách nhà hàng gần nhất thành công"));
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

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> toggleLock(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantLockRequest request) throws ResourceNotFoundException {
        RestaurantResponse response = restaurantService.toggleLockRestaurant(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái khóa nhà hàng thành công"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> approveRestaurant(@PathVariable Long id)
            throws ResourceNotFoundException {
        RestaurantResponse response = restaurantService.approveRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Phê duyệt đăng ký nhà hàng thành công"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> rejectRestaurant(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) throws ResourceNotFoundException {
        String reason = body != null ? body.get("reason") : null;
        RestaurantResponse response = restaurantService.rejectRestaurant(id, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Từ chối hồ sơ đăng ký nhà hàng thành công"));
    }
}
