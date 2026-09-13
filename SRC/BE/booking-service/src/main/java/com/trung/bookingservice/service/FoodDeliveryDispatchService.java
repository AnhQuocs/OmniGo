package com.trung.bookingservice.service;

import com.trung.bookingservice.event.FindDriverForFoodOrderEvent;
import com.trung.bookingservice.exception.BadRequestException;

public interface FoodDeliveryDispatchService {

    void dispatchDriverForFoodOrder(FindDriverForFoodOrderEvent event);

    void acceptFoodOrder(Long orderId, Long driverId) throws BadRequestException;

    void rejectFoodOrder(Long orderId, Long driverId);
}
