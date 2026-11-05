package com.trung.fooddeliveryservice.controller;
import com.trung.fooddeliveryservice.repository.FoodOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import com.trung.fooddeliveryservice.service.OrderCreationGuard;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/internal/financial")
public class FinancialOrderController {
    private final FoodOrderRepository repository;
    private final OrderCreationGuard creationGuard;
    @GetMapping("/attempts/{customer}/{key}")
    public Map<String,Object> attempt(@PathVariable Long customer, @PathVariable String key) { return creationGuard.attempt(customer, key); }
    @GetMapping("/customers/{customer}/orders") @Transactional(readOnly = true)
    public List<Map<String,Object>> customerOrders(@PathVariable Long customer) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customer).stream().map(order -> get(order.getId())).toList();
    }
    @GetMapping("/orders/{id}") @Transactional(readOnly = true)
    public Map<String,Object> get(@PathVariable Long id) {
        var order = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Map<String,Object> data = new HashMap<>();
        data.put("id", order.getId()); data.put("customerId", order.getCustomerId());
        data.put("driverId", order.getDriverId()); data.put("status", order.getStatus().name());
        data.put("paymentMethod", order.getPaymentMethod());
        data.put("amount", order.getTotalPrice());
        data.put("originalAmount", order.getOriginalAmount() == null ? order.getTotalPrice() : order.getOriginalAmount());
        data.put("discountAmount", order.getDiscountAmount());
        data.put("deliveryFee", order.getDeliveryFee());
        data.put("restaurantOwnerId", order.getRestaurant().getOwnerId());
        data.put("paid", order.getIsPaid());
        return data;
    }
}
