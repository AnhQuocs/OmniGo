package com.trung.bookingservice.controller;
import com.trung.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/internal/financial")
public class FinancialOrderController {
    private final BookingRepository repository;
    private final com.trung.bookingservice.service.impl.BookingServiceImpl bookings;
    @PostMapping("/orders/{id}/paid")
    public void paid(@PathVariable Long id) throws com.trung.bookingservice.exception.ResourceNotFoundException { bookings.markPrepaid(id); }
    private final com.trung.bookingservice.service.OrderCreationGuard creationGuard;
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
        data.put("amount", order.getPrice());
        data.put("originalAmount", order.getOriginalAmount() == null ? order.getPrice() : order.getOriginalAmount());
        data.put("discountAmount", order.getDiscountAmount());
        data.put("prepaid", order.isPrepaid()); data.put("paid", order.isPaid());
        return data;
    }
}
