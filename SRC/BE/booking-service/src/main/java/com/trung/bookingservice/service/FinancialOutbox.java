package com.trung.bookingservice.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
@Service @RequiredArgsConstructor @Slf4j
public class FinancialOutbox {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    @Value("${app.payment-service-url:http://localhost:8085}") private String paymentUrl;
    @Value("${app.internal-api-token:}") private String token;
    public void enqueue(Long orderId) {
        jdbc.update("insert into financial_outbox(order_id) values (?) on conflict (order_id) do nothing", orderId);
    }
    @Scheduled(fixedDelay = 5000)
    public void dispatch() {
        for (Long id : jdbc.queryForList("select order_id from financial_outbox where next_attempt_at <= CURRENT_TIMESTAMP order by next_attempt_at limit 50", Long.class)) {
            transactions.executeWithoutResult(status -> {
                var rows = jdbc.queryForList("select order_id from financial_outbox where order_id = ? for update skip locked", id);
                if (rows.isEmpty()) return;
                try {
                    if (token == null || token.isBlank()) throw new IllegalStateException("Missing INTERNAL_API_TOKEN");
                    var factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
                    HttpHeaders headers = new HttpHeaders(); headers.set("X-Internal-Token", token);
                    new RestTemplate(factory).exchange(paymentUrl + "/api/v1/payments/internal/reconcile/ride/" + id,
                            HttpMethod.POST, new HttpEntity<>(headers), Void.class);
                    jdbc.update("delete from financial_outbox where order_id = ?", id);
                } catch (Exception error) {
                    jdbc.update("update financial_outbox set attempts = attempts + 1, next_attempt_at = ?, last_error = ? where order_id = ?",
                            LocalDateTime.now().plusSeconds(30), error.getClass().getSimpleName(), id);
                    log.warn("Financial reconciliation pending for order {}: {}", id, error.getClass().getSimpleName());
                }
            });
        }
    }
}
