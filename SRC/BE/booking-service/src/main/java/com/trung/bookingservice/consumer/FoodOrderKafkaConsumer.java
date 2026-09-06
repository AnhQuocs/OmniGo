package com.trung.bookingservice.consumer;

import com.trung.bookingservice.event.FindDriverForFoodOrderEvent;
import com.trung.bookingservice.service.FoodDeliveryDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FoodOrderKafkaConsumer {

    private final FoodDeliveryDispatchService foodDeliveryDispatchService;

    @KafkaListener(topics = "FIND_DRIVER_FOR_FOOD_ORDER", groupId = "booking-service-group")
    public void handleFindDriverForFoodOrder(FindDriverForFoodOrderEvent event) {
        log.info("Nhận được yêu cầu tìm tài xế giao đồ ăn từ Kafka: orderId={}, restaurant={}",
                event.getOrderId(), event.getRestaurantName());
        try {
            foodDeliveryDispatchService.dispatchDriverForFoodOrder(event);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý điều phối tài xế giao đồ ăn cho đơn {}: {}", event.getOrderId(), e.getMessage(), e);
        }
    }
}
