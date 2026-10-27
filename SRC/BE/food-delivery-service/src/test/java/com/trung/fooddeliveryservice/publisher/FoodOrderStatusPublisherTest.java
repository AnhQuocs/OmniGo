package com.trung.fooddeliveryservice.publisher;

import com.trung.fooddeliveryservice.entity.FoodOrder;
import com.trung.fooddeliveryservice.event.FoodOrderStatusEvent;
import com.trung.fooddeliveryservice.util.enums.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FoodOrderStatusPublisherTest {
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, Object> kafka = mock(KafkaTemplate.class);
    private final FoodEventPublisher publisher = new FoodEventPublisher(kafka);

    @AfterEach
    void cleanup() { TransactionSynchronizationManager.clear(); }

    private FoodOrder order() {
        return FoodOrder.builder().id(1L).customerId(2L).driverId(3L).status(OrderStatus.DELIVERING).build();
    }

    private void begin() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    @Test
    void sendsSnapshotOnlyAfterCommit() {
        when(kafka.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
        begin();
        FoodOrder order = order();
        publisher.publishOrderStatusAfterCommit(order);
        order.setStatus(OrderStatus.CANCELLED);
        verifyNoInteractions(kafka);
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(kafka).send(argThat((ProducerRecord<String, Object> record) -> {
            FoodOrderStatusEvent event = (FoodOrderStatusEvent) record.value();
            return record.topic().equals("FOOD_ORDER_STATUS") && record.key().equals("1")
                    && event.getStatus().equals("DELIVERING")
                    && new String(record.headers().lastHeader("food-status-recipients").value(), StandardCharsets.UTF_8).equals("2,3") && event.getTimestamp().endsWith("Z");
        }));
    }

    @Test
    void kafkaJsonContainsOnlyThreePublicFields() throws Exception {
        var json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(
                new FoodOrderStatusEvent(1L, "PENDING", "2026-10-22T00:00:00Z")));
        assertEquals(3, json.size());
        assertEquals(1L, json.get("orderId").asLong());
        assertEquals("PENDING", json.get("status").asText());
        assertEquals("2026-10-22T00:00:00Z", json.get("timestamp").asText());
    }

    @Test
    void rollbackDoesNotPublish() {
        begin();
        publisher.publishOrderStatusAfterCommit(order());
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verifyNoInteractions(kafka);
    }

    @Test
    void alreadyCommittedTimeoutPathPublishesImmediatelyAndDoesNotFailOnBrokerError() {
        when(kafka.send(any(ProducerRecord.class))).thenThrow(new IllegalStateException("broker unavailable"));
        assertDoesNotThrow(() -> publisher.publishOrderStatusAfterCommit(order()));
        verify(kafka).send(any(ProducerRecord.class));
    }
}
