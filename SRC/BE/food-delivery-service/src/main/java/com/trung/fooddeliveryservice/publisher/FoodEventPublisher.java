package com.trung.fooddeliveryservice.publisher;

import com.trung.fooddeliveryservice.event.FindDriverForFoodOrderEvent;
import com.trung.fooddeliveryservice.event.FoodDriverRatedEvent;
import com.trung.fooddeliveryservice.event.FoodOrderStatusEvent;
import com.trung.fooddeliveryservice.entity.FoodOrder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    public static final String FOOD_ORDER_STATUS_TOPIC = "FOOD_ORDER_STATUS";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderStatusAfterCommit(FoodOrder order) {
        // Capture scalar values now; never access a detached entity from the callback.
        FoodOrderStatusEvent event = new FoodOrderStatusEvent(order.getId(),
                order.getStatus().name(), Instant.now().toString());
        String recipients = order.getCustomerId().toString();
        if (order.getDriverId() != null && !order.getDriverId().equals(order.getCustomerId())) {
            recipients += "," + order.getDriverId();
        }
        final byte[] routingHeader = recipients.getBytes(StandardCharsets.UTF_8);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendOrderStatus(event, routingHeader);
                }
            });
        } else {
            // Async timeout paths call this after repository.save has committed.
            sendOrderStatus(event, routingHeader);
        }
    }

    private void sendOrderStatus(FoodOrderStatusEvent event, byte[] routingHeader) {
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    FOOD_ORDER_STATUS_TOPIC, event.getOrderId().toString(), event);
            record.headers().add("food-status-recipients", routingHeader);
            kafkaTemplate.send(record)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Could not publish food order status: orderId={}", event.getOrderId(), error);
                        }
                    });
        } catch (Exception error) {
            // The DB commit already succeeded. Do not turn a notification failure into an HTTP failure.
            log.error("Could not publish food order status: orderId={}", event.getOrderId(), error);
        }
    }

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
