package com.trung.bookingservice.consumer;

import com.trung.bookingservice.event.FoodOrderStatusEvent;
import com.trung.bookingservice.dto.response.FoodOrderStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Header;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class FoodOrderStatusConsumer {
    public static final String DESTINATION = "/queue/food-order/status";
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "FOOD_ORDER_STATUS", groupId = "booking-service-group")
    public void handleStatus(FoodOrderStatusEvent event,
                             @Header("food-status-recipients") byte[] routingHeader) {
        if (event.getOrderId() == null || event.getStatus() == null || routingHeader == null) {
            throw new IllegalArgumentException("Food order status requires orderId, status and routing header");
        }
        String recipients = new String(routingHeader, StandardCharsets.UTF_8);
        if (!recipients.matches("[1-9][0-9]*(,[1-9][0-9]*)*")) {
            throw new IllegalArgumentException("Invalid food order status routing header");
        }
        var payload = new FoodOrderStatusResponse(event.getOrderId(), event.getStatus(), event.getTimestamp());
        Arrays.stream(recipients.split(",")).distinct()
                .forEach(recipient -> messagingTemplate.convertAndSendToUser(recipient, DESTINATION, payload));
    }
}
