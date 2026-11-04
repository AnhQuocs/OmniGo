package com.trung.fooddeliveryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Service
public class OrderCreationGuard {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules()
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    private final RestTemplate http;
    @Value("${app.payment-service-url:http://localhost:8085}") private String paymentUrl;
    @Value("${app.internal-api-token:}") private String token;
    public OrderCreationGuard(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc = jdbc; this.tx = new TransactionTemplate(manager);
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(15000);
        this.http = new RestTemplate(factory);
    }
    @FunctionalInterface public interface Action<T> { T run() throws Exception; }
    private void lock(Long customer, String key) {
        jdbc.queryForList("select pg_advisory_xact_lock(hashtextextended(?, 0))", "CREATE:" + customer + ":" + key);
    }
    public Map<String,Object> attempt(Long customer, String key) {
        var rows = jdbc.queryForList("select state, order_id from order_idempotency where customer_id=? and request_key=?", customer, key);
        return rows.isEmpty() ? Map.of("state", "UNKNOWN") : rows.get(0);
    }
    public <T> T create(Long customer, String key, Object request, Class<T> resultType, Function<T,Long> id, Action<T> action) {
        if (key == null || !key.matches("[A-Za-z0-9_-]{16,128}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu hoặc sai Idempotency-Key (16–128 ký tự)");
        final String fingerprint;
        try { fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.writeValueAsBytes(request))); }
        catch (Exception e) { throw new IllegalArgumentException("Không mã hóa được yêu cầu", e); }
        var admitted = new AtomicBoolean(false);
        try {
            return tx.execute(status -> {
                lock(customer, key);
                var previous = jdbc.queryForList("select * from order_idempotency where customer_id=? and request_key=?", customer, key);
                if (!previous.isEmpty()) {
                    var row = previous.get(0);
                    if (!fingerprint.equals(row.get("fingerprint"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key đã dùng với nội dung khác");
                    if ("FAILED".equals(row.get("state"))) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Yêu cầu trước đã thất bại; hãy bắt đầu yêu cầu mới");
                    try { return json.readValue((String)row.get("response"), resultType); }
                    catch (Exception e) { throw new IllegalStateException(e); }
                }
                if (token == null || token.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình xác thực nội bộ");
                var headers = new HttpHeaders(); headers.set("X-Internal-Token", token);
                try {
                    http.exchange(paymentUrl + "/api/v1/payments/internal/order-admission", HttpMethod.POST,
                        new HttpEntity<>(Map.of("customerId", customer, "key", key, "kind", "FOOD", "fingerprint", fingerprint), headers), Void.class);
                } catch (HttpStatusCodeException e) {
                    throw new ResponseStatusException(e.getStatusCode(), "Không thể đặt đơn: còn đơn chưa thanh toán hoặc yêu cầu đang xử lý. Hãy kiểm tra đơn cũ.");
                }
                admitted.set(true);
                try {
                    T result = action.run();
                    jdbc.update("insert into order_idempotency(customer_id,request_key,fingerprint,state,response,order_id) values (?,?,?,'SUCCESS',?,?)",
                        customer, key, fingerprint, json.writeValueAsString(result), id.apply(result));
                    return result;
                } catch (RuntimeException e) { throw e; }
                catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e); }
            });
        } catch (RuntimeException e) {
            // The business transaction rolled back. Persist its outcome separately so admission can recover after a restart.
            if (admitted.get()) {
                try { tx.executeWithoutResult(status -> {
                    lock(customer, key);
                    jdbc.update("insert into order_idempotency(customer_id,request_key,fingerprint,state) values (?,?,?,'FAILED') on conflict do nothing", customer, key, fingerprint);
                }); } catch (RuntimeException storageError) { e.addSuppressed(storageError); }
            }
            throw e;
        }
    }
}
