package com.trung.fooddeliveryservice.service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.server.ResponseStatusException;
@Service
public class OrderVoucherService {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate separate;
    private final RestTemplate http;
    @Value("${app.pricing-service-url:${PRICING_SERVICE_URL:http://localhost:8084}}") private String url;
    @Value("${app.internal-api-token:${INTERNAL_API_TOKEN:omnipay-internal-shared-secret-token-32bytes-min}}") private String token;
    public OrderVoucherService(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc=jdbc; separate=new TransactionTemplate(manager);
        separate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(3000);factory.setReadTimeout(10000);http=new RestTemplate(factory);
    }
    private HttpHeaders headers() { var headers=new HttpHeaders(); headers.set("X-Internal-Token",token); return headers; }
    public BigDecimal redeem(String code, Long customer, Long id, BigDecimal original) {
        if (code == null || code.isBlank()) return BigDecimal.ZERO;
        if (id == null) throw new IllegalStateException("Order must be persisted before redeeming a voucher");
        code=code.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z0-9_-]{1,50}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Mã voucher không hợp lệ");
        final String normalized=code;
        jdbc.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))", "VOUCHER_ORDER:"+id);
        separate.executeWithoutResult(s -> jdbc.update("insert into voucher_compensation(order_id,code,customer_id) values (?,?,?) on conflict do nothing",id,normalized,customer));
        final Map result;
        try {
            result=http.exchange(url + "/api/v1/pricing/internal/vouchers/redeem",HttpMethod.POST,
                new HttpEntity<>(Map.of("code",code,"userId",customer.toString(),"orderId","FOOD:"+id,
                    "serviceType","FOOD_DELIVERY","originalAmount",original),headers()),Map.class).getBody();
        } catch (org.springframework.web.client.RestClientException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Chưa xác minh được voucher. Vui lòng thử lại.");
        }
        if (result == null || !Boolean.TRUE.equals(result.get("valid")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,result == null ? "Voucher không hợp lệ" : String.valueOf(result.get("message")));
        BigDecimal discount=new BigDecimal(result.get("discountAmount").toString()).setScale(0,RoundingMode.UNNECESSARY);
        BigDecimal total=new BigDecimal(result.get("finalAmount").toString());
        if (discount.signum()<0 || discount.compareTo(original)>0 || original.subtract(discount).compareTo(total)!=0)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Kết quả voucher không hợp lệ");
        return discount;
    }
    @Scheduled(fixedDelay=30000)
    public void compensateRolledBackOrders() {
        for (var row:jdbc.queryForList("select * from voucher_compensation where check_after <= CURRENT_TIMESTAMP order by check_after limit 50")) {
            Long id=((Number)row.get("order_id")).longValue();
            separate.executeWithoutResult(status -> {
                if (!Boolean.TRUE.equals(jdbc.queryForObject("select pg_try_advisory_xact_lock(hashtextextended(?,0))", Boolean.class, "VOUCHER_ORDER:"+id))) return;
                try {
                    var orders = jdbc.queryForList("select status from food_orders where id=?", id);
                    String state = orders.isEmpty() ? "MISSING" : String.valueOf(orders.get(0).get("status"));
                    if (Set.of("MISSING", "CANCELLED", "CANCELED", "REJECTED").contains(state)) {
                        http.exchange(url+"/api/v1/pricing/internal/vouchers/release",HttpMethod.POST,
                            new HttpEntity<>(Map.of("code",row.get("code"),"userId",row.get("customer_id").toString(),"orderId","FOOD:"+id),headers()),Void.class);
                    } else if (!"COMPLETED".equals(state)) {
                        jdbc.update("update voucher_compensation set check_after=CURRENT_TIMESTAMP + interval '30 seconds' where order_id=?",id);
                        return;
                    }
                    jdbc.update("delete from voucher_compensation where order_id=?",id);
                } catch (Exception retryLater) {
                    jdbc.update("update voucher_compensation set check_after=CURRENT_TIMESTAMP + interval '30 seconds' where order_id=?",id);
                }
            });
        }
    }
}
