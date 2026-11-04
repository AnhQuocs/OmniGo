package com.trung.bookingservice.service;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class OrderCreationGuardIntegrationTest {
    JdbcTemplate jdbc; String schema; OrderCreationGuard guard; HttpServer server;
    AtomicInteger admissions; AtomicInteger admissionStatus;
    public record Result(Long id, String name) {}
    @BeforeEach void setup() throws Exception {
        String url = System.getenv("TEST_POSTGRES_URL"); Assumptions.assumeTrue(url != null);
        var ds = new DriverManagerDataSource(url, "codex_test", "");
        schema = "idem_" + UUID.randomUUID().toString().replace("-", "");
        new JdbcTemplate(ds).execute("create schema " + schema);
        ds.setUrl(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema);
        jdbc = new JdbcTemplate(ds);
        jdbc.execute("create table order_idempotency(customer_id bigint,request_key varchar(128),fingerprint varchar(64),state varchar(16),response text,order_id bigint,primary key(customer_id,request_key))");
        jdbc.execute("create table test_orders(id bigint primary key)");
        admissions = new AtomicInteger(); admissionStatus = new AtomicInteger(204);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/payments/internal/order-admission", exchange -> {
            admissions.incrementAndGet(); exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(admissionStatus.get(), -1); exchange.close();
        }); server.start();
        guard = new OrderCreationGuard(jdbc, new DataSourceTransactionManager(ds));
        ReflectionTestUtils.setField(guard, "paymentUrl", "http://127.0.0.1:" + server.getAddress().getPort());
        ReflectionTestUtils.setField(guard, "token", "test-secret");
    }
    @AfterEach void close() { if (server != null) server.stop(0); if (jdbc != null) jdbc.execute("drop schema " + schema + " cascade"); }
    @Test void concurrentRetryCreatesExactlyOneOrderAndReplaysAfterRestart() throws Exception {
        var count = new AtomicInteger(); var pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Result>> calls = new ArrayList<>();
            for (int i=0;i<8;i++) calls.add(() -> guard.create(7L,"same-key-123456789", Map.of("amount",100), Result.class, Result::id, () -> {
                count.incrementAndGet(); jdbc.update("insert into test_orders values (1)"); return new Result(1L,"original");
            }));
            for (var result : pool.invokeAll(calls)) assertEquals(1L,result.get().id());
            assertEquals(1,count.get()); assertEquals(1, admissions.get());
            assertEquals("original", guard.create(7L,"same-key-123456789",Map.of("amount",100),Result.class,Result::id,()->{throw new AssertionError();}).name());
        } finally { pool.shutdownNow(); }
    }
    @Test void keyWithChangedPayloadIsRejected() {
        guard.create(7L,"same-key-123456789",Map.of("amount",100),Result.class,Result::id,()->new Result(1L,"first"));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> guard.create(7L,"same-key-123456789",Map.of("amount",200),Result.class,Result::id,()->new Result(2L,"bad"))).getStatusCode().value());
    }
    @Test void businessFailureRollsBackOrderAndPersistsFailedAttempt() {
        assertThrows(RuntimeException.class, () -> guard.create(7L,"same-key-123456789",Map.of(),Result.class,Result::id,()-> {
            jdbc.update("insert into test_orders values (1)"); throw new IllegalStateException("failure");
        }));
        assertEquals(0,jdbc.queryForObject("select count(*) from test_orders",Integer.class));
        assertEquals("FAILED", guard.attempt(7L,"same-key-123456789").get("state"));
        assertEquals(422, assertThrows(ResponseStatusException.class, () -> guard.create(7L,"same-key-123456789",Map.of(),Result.class,Result::id,()->new Result(2L,"bad"))).getStatusCode().value());
    }
    @Test void admissionFailureDoesNotCreateOrderAndSameKeyCanRecover() {
        admissionStatus.set(503);
        assertThrows(ResponseStatusException.class, () -> guard.create(7L,"same-key-123456789",Map.of(),Result.class,Result::id,()->{throw new AssertionError();}));
        assertEquals("UNKNOWN",guard.attempt(7L,"same-key-123456789").get("state"));
        admissionStatus.set(204);
        assertEquals(1L, guard.create(7L,"same-key-123456789",Map.of(),Result.class,Result::id,()->new Result(1L,"recovered")).id());
    }
}
