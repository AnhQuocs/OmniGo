package com.trung.fooddeliveryservice.consumer;

import com.trung.fooddeliveryservice.event.DriverAssignedToFoodOrderEvent;
import com.trung.fooddeliveryservice.service.FoodOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FoodOrderDriverAssignedConsumer {

    private final FoodOrderService foodOrderService;

    @KafkaListener(topics = "DRIVER_ASSIGNED_TO_FOOD_ORDER", groupId = "food-delivery-service-group")
    public void handleDriverAssigned(DriverAssignedToFoodOrderEvent event) {
        log.info("Nhận event DRIVER_ASSIGNED_TO_FOOD_ORDER từ Kafka: orderId={}, driverId={}",
                event.getOrderId(), event.getDriverId());
        try {
            foodOrderService.assignDriver(event.getOrderId(), event.getDriverId());
            log.info("Gán thành công tài xế ID {} vào đơn hàng ID {}", event.getDriverId(), event.getOrderId());
        } catch (Exception e) {
            log.error("Lỗi khi gán tài xế vào đơn hàng {}: {}", event.getOrderId(), e.getMessage(), e);
        }
    }
}
