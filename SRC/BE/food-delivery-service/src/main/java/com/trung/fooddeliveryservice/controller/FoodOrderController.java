package com.trung.fooddeliveryservice.controller;

import com.trung.fooddeliveryservice.dto.request.FoodOrderCreateRequest;
import com.trung.fooddeliveryservice.dto.request.FoodOrderStatusUpdateRequest;
import com.trung.fooddeliveryservice.dto.response.ApiResponse;
import com.trung.fooddeliveryservice.dto.response.FoodOrderResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.service.FoodOrderService;
import com.trung.fooddeliveryservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/food-orders")
@RequiredArgsConstructor
public class FoodOrderController {

    private final FoodOrderService foodOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<FoodOrderResponse>> createOrder(
            @Valid @RequestBody FoodOrderCreateRequest request) throws ResourceNotFoundException, BadRequestException, UnauthorizedException {
        Long customerId = SecurityUtils.getCurrentUserId();
        FoodOrderResponse response = foodOrderService.createOrder(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đặt món thành công, hệ thống đang tìm tài xế giao hàng"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<FoodOrderResponse>> getOrderById(
            @PathVariable Long orderId) throws ResourceNotFoundException {
        FoodOrderResponse response = foodOrderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin đơn hàng thành công"));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<FoodOrderResponse>>> getMyOrders()
            throws UnauthorizedException {
        Long customerId = SecurityUtils.getCurrentUserId();
        List<FoodOrderResponse> list = foodOrderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy lịch sử đơn hàng của bạn thành công"));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<FoodOrderResponse>>> getRestaurantOrders(
            @PathVariable Long restaurantId) throws ResourceNotFoundException, UnauthorizedException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        List<FoodOrderResponse> list = foodOrderService.getRestaurantOrders(restaurantId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách đơn của nhà hàng thành công"));
    }

    @GetMapping("/driver")
    public ResponseEntity<ApiResponse<List<FoodOrderResponse>>> getDriverOrders()
            throws UnauthorizedException {
        Long driverId = SecurityUtils.getCurrentUserId();
        List<FoodOrderResponse> list = foodOrderService.getDriverOrders(driverId);
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách đơn giao của tài xế thành công"));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<FoodOrderResponse>> updateOrderStatusByRestaurant(
            @PathVariable Long orderId,
            @Valid @RequestBody FoodOrderStatusUpdateRequest request)
            throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        Long ownerId = SecurityUtils.getCurrentUserId();
        FoodOrderResponse response = foodOrderService.updateOrderStatusByRestaurant(
                orderId, request.getStatus(), ownerId, request.getReason(), request.getReasonCode());
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái đơn hàng thành công"));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<FoodOrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) com.trung.fooddeliveryservice.dto.request.FoodOrderCancelRequest cancelRequest)
            throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        Long customerId = SecurityUtils.getCurrentUserId();
        String reason = cancelRequest != null ? cancelRequest.getReason() : null;
        com.trung.fooddeliveryservice.util.enums.OrderCancelReason reasonCode = cancelRequest != null ? cancelRequest.getReasonCode() : null;
        FoodOrderResponse response = foodOrderService.cancelOrderByCustomer(orderId, customerId, reason, reasonCode);
        return ResponseEntity.ok(ApiResponse.success(response, "Hủy đơn hàng thành công"));
    }

    @PatchMapping("/{orderId}/driver-status")
    public ResponseEntity<ApiResponse<FoodOrderResponse>> updateOrderStatusByDriver(
            @PathVariable Long orderId,
            @Valid @RequestBody FoodOrderStatusUpdateRequest request)
            throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        Long driverId = SecurityUtils.getCurrentUserId();
        FoodOrderResponse response = foodOrderService.updateOrderStatusByDriver(
                orderId, request.getStatus(), driverId, request.getReason(), request.getReasonCode());
        return ResponseEntity.ok(ApiResponse.success(response, "Tài xế cập nhật trạng thái đơn hàng thành công"));
    }

    @RequestMapping(value = "/{orderId}/paid", method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<FoodOrderResponse>> markOrderAsPaid(
            @PathVariable Long orderId) throws ResourceNotFoundException {
        FoodOrderResponse response = foodOrderService.markOrderAsPaid(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã cập nhật trạng thái thanh toán đơn hàng"));
    }

    @PatchMapping("/{orderId}/switch-to-cash")
    public ResponseEntity<ApiResponse<FoodOrderResponse>> switchToCash(
            @PathVariable Long orderId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        Long customerId = SecurityUtils.getCurrentUserId();
        FoodOrderResponse response = foodOrderService.switchToCashPayment(orderId, customerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã chuyển đổi sang phương thức thanh toán tiền mặt thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodOrderResponse>>> getAllOrders() {
        List<FoodOrderResponse> list = foodOrderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy toàn bộ danh sách đơn đặt món thành công"));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getAdminFoodStats() {
        java.util.Map<String, Object> stats = foodOrderService.getAdminFoodStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê đồ ăn thành công"));
    }
}
