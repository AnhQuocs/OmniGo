package com.trung.paymentservice.service;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.server.ResponseStatusException;
@Service
public class FinancialOrderClient {
    @Value("${app.food-delivery-url:http://localhost:8086}") private String foodUrl;
    @Value("${app.booking-url:http://localhost:8083}") private String rideUrl;
    @Value("${app.internal-api-token:}") private String token;
    private final RestTemplate http;
    public FinancialOrderClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000); http = new RestTemplate(factory);
    }
    private HttpHeaders headers() {
        if (token == null || token.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình xác thực nội bộ");
        HttpHeaders headers = new HttpHeaders(); headers.set("X-Internal-Token", token); return headers;
    }
    public Map<String,Object> get(boolean food, Long id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu mã đơn");
        return http.exchange((food ? foodUrl : rideUrl) + "/api/v1/internal/financial/orders/" + id,
                HttpMethod.GET, new HttpEntity<>(headers()), Map.class).getBody();
    }
    public Map<String,Object> attempt(boolean food, Long customer, String key) {
        return http.exchange((food ? foodUrl : rideUrl) + "/api/v1/internal/financial/attempts/" + customer + "/" + key,
            HttpMethod.GET, new HttpEntity<>(headers()), Map.class).getBody();
    }
    public List<Map<String,Object>> customerOrders(boolean food, Long customer) {
        var result = http.exchange((food ? foodUrl : rideUrl) + "/api/v1/internal/financial/customers/" + customer + "/orders",
            HttpMethod.GET, new HttpEntity<>(headers()), new org.springframework.core.ParameterizedTypeReference<List<Map<String,Object>>>() {}).getBody();
        if (result == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa kiểm tra được đơn cũ");
        return result;
    }
    public void markFoodPaid(Long id) {
        http.exchange(foodUrl + "/api/v1/food-orders/" + id + "/paid", HttpMethod.POST, new HttpEntity<>(headers()), Void.class);
    }
    public static Long id(Map<String,Object> order, String key) {
        Object value = order.get(key); return value == null ? null : ((Number)value).longValue();
    }
    public static BigDecimal money(Map<String,Object> order, String key) { return new BigDecimal(order.get(key).toString()); }
    public static boolean cancelled(Map<String,Object> order) { return Set.of("CANCELLED", "CANCELED", "REJECTED").contains(order.get("status")); }
    public static boolean cash(Map<String,Object> order) { return "CASH".equalsIgnoreCase(String.valueOf(order.get("paymentMethod"))); }
}
