package com.trung.bookingservice.consumer;

import com.trung.bookingservice.event.FoodOrderStatusEvent;
import com.trung.bookingservice.dto.response.FoodOrderStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FoodOrderStatusConsumer {
    public static final String DESTINATION = "/queue/food-order/status";
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "FOOD_ORDER_STATUS", groupId = "booking-service-group")
    public void handleStatus(FoodOrderStatusEvent event) {
        if (event.getOrderId() == null || event.getCustomerId() == null || event.getStatus() == null) {
            throw new IllegalArgumentException("Food order status requires orderId, customerId and status");
        }
        var payload = new FoodOrderStatusResponse(event.getOrderId(), event.getStatus(), event.getTimestamp());
        messagingTemplate.convertAndSendToUser(event.getCustomerId().toString(), DESTINATION, payload);
        if (event.getDriverId() != null && !event.getDriverId().equals(event.getCustomerId())) {
            messagingTemplate.convertAndSendToUser(event.getDriverId().toString(), DESTINATION, payload);
        }
    }
}
