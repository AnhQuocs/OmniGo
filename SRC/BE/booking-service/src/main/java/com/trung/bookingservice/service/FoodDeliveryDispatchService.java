package com.trung.bookingservice.service;

import com.trung.bookingservice.event.FindDriverForFoodOrderEvent;

public interface FoodDeliveryDispatchService {

    void dispatchDriverForFoodOrder(FindDriverForFoodOrderEvent event);

    void acceptFoodOrder(Long orderId, Long driverId) throws com.trung.bookingservice.exception.BadRequestException;

    void rejectFoodOrder(Long orderId, Long driverId);
}
