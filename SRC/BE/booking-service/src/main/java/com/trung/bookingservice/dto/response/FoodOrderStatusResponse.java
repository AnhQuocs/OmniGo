package com.trung.bookingservice.dto.response;

/** Public STOMP payload; recipient IDs stay in the internal Kafka event. */
public record FoodOrderStatusResponse(Long orderId, String status, String timestamp) {
}