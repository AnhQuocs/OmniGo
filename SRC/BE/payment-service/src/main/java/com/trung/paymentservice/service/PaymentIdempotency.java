package com.trung.paymentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.util.enums.TransactionStatus;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.security.MessageDigest;
import java.math.BigDecimal;

@Service
public class PaymentIdempotency {
    record Context(String scope, String key) {}
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    private final JdbcTemplate jdbc;
    private final PaymentLocks locks;
    private final PaymentRequestService requests;
    private final TransactionRepository transactions;
    private final TransactionTemplate tx;
    private final TransactionTemplate independent;
    private final ObjectMapper json = new ObjectMapper();

    public PaymentIdempotency(JdbcTemplate jdbc, PaymentLocks locks, PaymentRequestService requests,
            TransactionRepository transactions, PlatformTransactionManager manager) {
        this.jdbc = jdbc; this.locks = locks; this.requests = requests; this.transactions = transactions;
        tx = new TransactionTemplate(manager); independent = new TransactionTemplate(manager);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    // Called inside the independently committed MoMo pending transaction, before sending any provider request.
    public static void bindProviderReference(JdbcTemplate jdbc, String reference) {
        Context context = CURRENT.get();
        if (context != null) jdbc.update("update payment_idempotency set provider_reference=? where scope=? and request_key=?",
            reference, context.scope(), context.key());
    }
    public PaymentUrlResponse create(String key, Long user, String role, String method, String type, Long order, BigDecimal amount) {
        if (key == null || !key.matches("[A-Za-z0-9_-]{16,128}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu hoặc sai Idempotency-Key");
        String scope = role.replace("ROLE_", "").toUpperCase(Locale.ROOT) + ":" + user;
        String fingerprint;
        try {
            fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.writeValueAsBytes(
                Arrays.asList(method.toUpperCase(Locale.ROOT), type.toUpperCase(Locale.ROOT), order, amount.stripTrailingZeros().toPlainString()))));
        } catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu thanh toán không hợp lệ", e); }
        return tx.execute(status -> {
            locks.lock("IDEMPOTENCY:" + scope + ":" + key);
            independent.executeWithoutResult(s -> jdbc.update("insert into payment_idempotency(scope,request_key,fingerprint) values (?,?,?) on conflict do nothing", scope, key, fingerprint));
            var row = jdbc.queryForMap("select * from payment_idempotency where scope=? and request_key=?", scope, key);
            if (!fingerprint.equals(row.get("fingerprint"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key đã dùng với nội dung khác");
            if (row.get("response") != null) {
                try {
                    var saved = json.readValue((String)row.get("response"), PaymentUrlResponse.class);
                    checkAttempt(saved.getOrderId());
                    return saved;
                }
                catch (ResponseStatusException e) { throw e; }
                catch (Exception e) { throw new IllegalStateException(e); }
            }
            if (row.get("provider_reference") != null) {
                checkAttempt((String)row.get("provider_reference"));
                var payment = transactions.findByOrderId((String)row.get("provider_reference")).orElseThrow();
                if (payment.getStatus() == TransactionStatus.SUCCESS)
                    return PaymentUrlResponse.builder().orderId(payment.getOrderId()).paymentUrl("ALREADY_PAID").build();
                if (payment.getStatus() == TransactionStatus.PENDING && payment.getPaymentUrl() != null)
                    return PaymentUrlResponse.builder().orderId(payment.getOrderId()).paymentUrl(payment.getPaymentUrl()).build();
                // A provider timeout is ambiguous. Never submit another charge with this key.
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Giao dịch đã gửi tới cổng thanh toán. Hãy kiểm tra trạng thái; không tạo giao dịch trùng.");
            }
            CURRENT.set(new Context(scope, key));
            try {
                var result = requests.create(user, role, method, type, order, amount);
                jdbc.update("update payment_idempotency set response=? where scope=? and request_key=?", json.writeValueAsString(result), scope, key);
                return result;
            } catch (RuntimeException e) { throw e; }
            catch (Exception e) { throw new IllegalStateException(e); }
            finally { CURRENT.remove(); }
        });
    }
    private void checkAttempt(String reference) {
        var payment = transactions.findByOrderId(reference).orElseThrow();
        if (payment.getStatus() == TransactionStatus.FAILED)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cổng thanh toán đã xác nhận thất bại. Có thể bắt đầu lần thanh toán mới.");
        if (payment.getStatus() == TransactionStatus.CANCELED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đã hết thời gian chờ nhưng chưa có kết quả từ cổng thanh toán. Cần đối soát trước khi trả lại.");
        if (transactions.findByOrderId("REFUND_PAYMENT_" + payment.getId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Giao dịch đã hoàn tiền; kiểm tra trạng thái đơn thay vì thanh toán lại.");
    }

}
