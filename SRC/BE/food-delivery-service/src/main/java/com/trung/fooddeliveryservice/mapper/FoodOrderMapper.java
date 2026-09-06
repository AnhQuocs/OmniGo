package com.trung.fooddeliveryservice.mapper;

import com.trung.fooddeliveryservice.dto.response.FoodOrderItemResponse;
import com.trung.fooddeliveryservice.dto.response.FoodOrderResponse;
import com.trung.fooddeliveryservice.entity.FoodOrder;
import com.trung.fooddeliveryservice.entity.FoodOrderItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FoodOrderMapper {

    public FoodOrderItemResponse toItemResponse(FoodOrderItem item) {
        if (item == null) return null;
        return FoodOrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    public List<FoodOrderItemResponse> toItemResponseList(List<FoodOrderItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    public FoodOrderResponse toResponse(FoodOrder order) {
        if (order == null) return null;
        return FoodOrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .restaurantId(order.getRestaurant() != null ? order.getRestaurant().getId() : null)
                .restaurantName(order.getRestaurant() != null ? order.getRestaurant().getName() : null)
                .restaurantAddress(order.getRestaurant() != null ? order.getRestaurant().getAddress() : null)
                .driverId(order.getDriverId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(order.getDeliveryFee())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryLatitude(order.getDeliveryLatitude())
                .deliveryLongitude(order.getDeliveryLongitude())
                .note(order.getNote())
                .paymentMethod(order.getPaymentMethod())
                .isPaid(order.getIsPaid())
                .orderItems(toItemResponseList(order.getOrderItems()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<FoodOrderResponse> toResponseList(List<FoodOrder> orders) {
        if (orders == null) return Collections.emptyList();
        return orders.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
