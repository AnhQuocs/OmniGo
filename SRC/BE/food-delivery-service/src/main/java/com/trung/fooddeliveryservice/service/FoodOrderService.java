package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.request.FoodOrderCreateRequest;
import com.trung.fooddeliveryservice.dto.response.FoodOrderResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.util.enums.OrderStatus;

import java.util.List;

public interface FoodOrderService {

    FoodOrderResponse createOrder(FoodOrderCreateRequest request, Long customerId) throws ResourceNotFoundException, BadRequestException;

    FoodOrderResponse getOrderById(Long orderId) throws ResourceNotFoundException;

    List<FoodOrderResponse> getCustomerOrders(Long customerId);

    List<FoodOrderResponse> getRestaurantOrders(Long restaurantId, Long ownerId) throws ResourceNotFoundException, UnauthorizedException;

    List<FoodOrderResponse> getDriverOrders(Long driverId);

    FoodOrderResponse updateOrderStatusByRestaurant(Long orderId, OrderStatus newStatus, Long ownerId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException;

    FoodOrderResponse cancelOrderByCustomer(Long orderId, Long customerId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException;

    FoodOrderResponse assignDriver(Long orderId, Long driverId) throws ResourceNotFoundException;

    FoodOrderResponse updateOrderStatusByDriver(Long orderId, OrderStatus newStatus, Long driverId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException;

    FoodOrderResponse markOrderAsPaid(Long orderId) throws ResourceNotFoundException;

    List<FoodOrderResponse> getAllOrders();

    java.util.Map<String, Object> getAdminFoodStats();
}
