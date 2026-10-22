package com.trung.bookingservice.consumer;

import com.trung.bookingservice.event.FoodOrderStatusEvent;
import com.trung.bookingservice.dto.response.FoodOrderStatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FoodOrderStatusConsumerTest {
    private final SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
    private final FoodOrderStatusConsumer consumer = new FoodOrderStatusConsumer(messaging);

    @Test
    void sendsPublicPayloadOnlyToCustomerAndAssignedDriver() {
        var event = new FoodOrderStatusEvent(1L, 2L, 3L, "DELIVERING", "2026-10-22T00:00:00Z");
        consumer.handleStatus(event);
        var payload = new FoodOrderStatusResponse(1L, "DELIVERING", event.getTimestamp());
        verify(messaging).convertAndSendToUser("2", "/queue/food-order/status", payload);
        verify(messaging).convertAndSendToUser("3", "/queue/food-order/status", payload);
        verifyNoMoreInteractions(messaging);
    }

    @Test
    void serializedPayloadHasExactlyThreeFields() throws Exception {
        var json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(
                new FoodOrderStatusResponse(1L, "PENDING", "2026-10-22T00:00:00Z")));
        assertEquals(3, json.size());
        assertEquals(1L, json.get("orderId").asLong());
        assertEquals("PENDING", json.get("status").asText());
        assertEquals("2026-10-22T00:00:00Z", json.get("timestamp").asText());
    }

    @Test
    void unassignedOrderNotifiesCustomerOnly() {
        var event = new FoodOrderStatusEvent(1L, 2L, null, "PENDING", "2026-10-22T00:00:00Z");
        consumer.handleStatus(event);
        verify(messaging).convertAndSendToUser("2", "/queue/food-order/status",
                new FoodOrderStatusResponse(1L, "PENDING", event.getTimestamp()));
        verifyNoMoreInteractions(messaging);
    }

    @Test
    void invalidEventDoesNotSend() {
        assertThrows(IllegalArgumentException.class, () -> consumer.handleStatus(new FoodOrderStatusEvent()));
        verifyNoInteractions(messaging);
    }
}