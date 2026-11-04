package com.trung.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.util.enums.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class OrderAdmissionService {
    private final JdbcTemplate jdbc;
    private final PaymentLocks locks;
    private final FinancialOrderClient orders;
    private final TransactionRepository transactions;

    @Transactional
    public void reserve(Long customer, String kind, String key, String fingerprint) {
        if (customer == null || customer <= 0 || !Set.of("RIDE", "FOOD").contains(kind)
                || key == null || !key.matches("[A-Za-z0-9_-]{16,128}")
                || fingerprint == null || !fingerprint.matches("[a-f0-9]{64}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu giữ chỗ không hợp lệ");
        // Both order services contend on the same durable customer lock, including across devices.
        locks.lock("CUSTOMER_ORDER:" + customer);
        var rows = jdbc.queryForList("select * from customer_order_admission where customer_id=?", customer);
        if (!rows.isEmpty()) {
            var previous = rows.get(0);
            if (key.equals(previous.get("request_key")) && kind.equals(previous.get("kind"))) {
                if (!fingerprint.equals(previous.get("fingerprint"))) throw conflict("Idempotency-Key đã dùng với nội dung khác");
                return; // Recover the original request, never reserve a second order.
            }
            var attempt = orders.attempt("FOOD".equals(previous.get("kind")), customer, (String)previous.get("request_key"));
            if (!Set.of("SUCCESS", "FAILED").contains(attempt.get("state")))
                throw conflict("Đơn trước đang được tạo. Gửi lại cùng Idempotency-Key để khôi phục.");
        }
        // Include pre-migration orders; do not rely solely on the new reservation table.
        for (boolean food : new boolean[]{false, true}) {
            for (var order : orders.customerOrders(food, customer)) {
                if (FinancialOrderClient.cancelled(order)) continue;
                if (FinancialOrderClient.cash(order) && "COMPLETED".equals(order.get("status"))) continue;
                if (!FinancialOrderClient.cash(order) && paid(food, FinancialOrderClient.id(order, "id"))) continue;
                throw conflict("Còn đơn #" + order.get("id") + " chưa hoàn tất thanh toán");
            }
        }
        jdbc.update("insert into customer_order_admission(customer_id,kind,request_key,fingerprint) values (?,?,?,?) "
                + "on conflict(customer_id) do update set kind=excluded.kind, request_key=excluded.request_key, fingerprint=excluded.fingerprint, created_at=current_timestamp",
                customer, kind, key, fingerprint);
    }
    private boolean paid(boolean food, Long id) {
        return transactions.findByBookingIdAndTransactionTypeOrderByIdDesc(id, food ? TransactionType.FOOD_PAYMENT : TransactionType.TRIP_PAYMENT)
            .stream().anyMatch(t -> t.getStatus() == TransactionStatus.SUCCESS
                && transactions.findByOrderId("REFUND_PAYMENT_" + t.getId()).isEmpty());
    }
    private ResponseStatusException conflict(String reason) { return new ResponseStatusException(HttpStatus.CONFLICT, reason); }
}
