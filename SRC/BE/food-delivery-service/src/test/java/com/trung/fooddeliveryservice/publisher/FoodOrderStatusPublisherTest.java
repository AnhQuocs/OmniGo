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
        when(kafka.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
        begin();
        FoodOrder order = order();
        publisher.publishOrderStatusAfterCommit(order);
        order.setStatus(OrderStatus.CANCELLED);
        verifyNoInteractions(kafka);
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(kafka).send(eq("FOOD_ORDER_STATUS"), eq("1"), argThat(value -> {
            FoodOrderStatusEvent event = (FoodOrderStatusEvent) value;
            return event.getStatus().equals("DELIVERING") && event.getCustomerId().equals(2L)
                    && event.getDriverId().equals(3L) && event.getTimestamp().endsWith("Z");
        }));
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
        when(kafka.send(anyString(), anyString(), any())).thenThrow(new IllegalStateException("broker unavailable"));
        assertDoesNotThrow(() -> publisher.publishOrderStatusAfterCommit(order()));
        verify(kafka).send(eq("FOOD_ORDER_STATUS"), eq("1"), any(FoodOrderStatusEvent.class));
    }
}
