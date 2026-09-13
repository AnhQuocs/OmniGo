package com.trung.fooddeliveryservice.publisher;

import com.trung.fooddeliveryservice.event.FindDriverForFoodOrderEvent;
import com.trung.fooddeliveryservice.event.FoodDriverRatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class FoodEventPublisher {

    public static final String FIND_DRIVER_TOPIC = "FIND_DRIVER_FOR_FOOD_ORDER";
    public static final String FOOD_DRIVER_RATED_TOPIC = "FOOD_DRIVER_RATED_TOPIC";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleFindDriverEvent(FindDriverForFoodOrderEvent event) {
        log.info("Publishing FindDriverForFoodOrderEvent to Kafka topic '{}': orderId={}", FIND_DRIVER_TOPIC, event.getOrderId());
        kafkaTemplate.send(FIND_DRIVER_TOPIC, String.valueOf(event.getOrderId()), event);
    }

    public void publishFindDriverDirect(FindDriverForFoodOrderEvent event) {
        log.info("Publishing direct FindDriverForFoodOrderEvent to Kafka topic '{}': orderId={}", FIND_DRIVER_TOPIC, event.getOrderId());
        kafkaTemplate.send(FIND_DRIVER_TOPIC, String.valueOf(event.getOrderId()), event);
    }

    public void publishDriverRatedDirect(FoodDriverRatedEvent event) {
        log.info("Publishing FoodDriverRatedEvent to Kafka topic '{}': orderId={}, driverId={}, rating={}",
                FOOD_DRIVER_RATED_TOPIC, event.getOrderId(), event.getDriverId(), event.getRating());
        kafkaTemplate.send(FOOD_DRIVER_RATED_TOPIC, String.valueOf(event.getDriverId()), event);
    }
}
