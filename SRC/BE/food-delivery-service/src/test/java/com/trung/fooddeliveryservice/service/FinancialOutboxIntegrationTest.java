package com.trung.fooddeliveryservice.service;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.core.io.ClassPathResource;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
@EnabledIfEnvironmentVariable(named="TEST_POSTGRES_URL",matches=".+")
class FinancialOutboxIntegrationTest {
    @Test void rollbackRetainsAtomicityAndFailedDeliverySurvivesUntilRetry() throws Exception {
        String url=System.getenv("TEST_POSTGRES_URL");
        var admin=new JdbcTemplate(new DriverManagerDataSource(url,"codex_test",""));
        String schema="test_outbox_fooddeliveryservice";
        admin.execute("create schema if not exists " + schema);
        var ds=new DriverManagerDataSource(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema,"codex_test","");
        var jdbc=new JdbcTemplate(ds); var tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        AtomicInteger code=new AtomicInteger(503),calls=new AtomicInteger();
        server.createContext("/api/v1/payments/internal/reconcile/", exchange -> {
            calls.incrementAndGet();
            int result="test-token".equals(exchange.getRequestHeaders().getFirst("X-Internal-Token")) ? code.get() : 403;
            exchange.sendResponseHeaders(result,-1);exchange.close();
        });
        server.start();
        try {
            try(var input=new ClassPathResource("db/migration/V20__financial_outbox.sql").getInputStream()) {
                jdbc.execute(new String(input.readAllBytes(),StandardCharsets.UTF_8));
            }
            final var outbox=new FinancialOutbox(jdbc,tx);
            ReflectionTestUtils.setField(outbox,"paymentUrl","http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(outbox,"token","test-token");
            tx.executeWithoutResult(status -> { outbox.enqueue(99L);status.setRollbackOnly(); });
            assertEquals(0,jdbc.queryForObject("select count(*) from financial_outbox",Integer.class));
            tx.executeWithoutResult(status -> { outbox.enqueue(99L);outbox.enqueue(99L); });
            assertEquals(1,jdbc.queryForObject("select count(*) from financial_outbox",Integer.class));
            outbox.dispatch();
            assertEquals(1,jdbc.queryForObject("select attempts from financial_outbox where order_id=99",Integer.class));
            assertEquals(1,calls.get());
            // Recreate worker to prove retry state is persisted, not kept in memory.
            var restarted=new FinancialOutbox(jdbc,tx);
            ReflectionTestUtils.setField(outbox,"paymentUrl","http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(outbox,"token","test-token");
            ReflectionTestUtils.setField(restarted,"paymentUrl","http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(restarted,"token","test-token");
            jdbc.update("update financial_outbox set next_attempt_at=CURRENT_TIMESTAMP");code.set(204);
            restarted.dispatch();restarted.dispatch();
            assertEquals(0,jdbc.queryForObject("select count(*) from financial_outbox",Integer.class));assertEquals(2,calls.get());
        } finally { server.stop(0);admin.execute("drop schema " + schema + " cascade"); }
    }
}
