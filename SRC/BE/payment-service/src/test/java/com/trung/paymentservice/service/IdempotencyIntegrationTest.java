package com.trung.paymentservice.service;

import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.util.enums.TransactionStatus;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdempotencyIntegrationTest {
    JdbcTemplate jdbc; String schema; DataSourceTransactionManager manager;
    PaymentIdempotency idempotency; PaymentRequestService requests; TransactionRepository transactions;
    OrderAdmissionService admission; FinancialOrderClient orders; TransactionTemplate tx;
    final String key = "unique-request-key-12345";
    final String fingerprint = "a".repeat(64);
    @BeforeEach void setup() {
        String url = System.getenv("TEST_POSTGRES_URL"); Assumptions.assumeTrue(url != null);
        var ds = new DriverManagerDataSource(url,"codex_test","");
        schema = "idem_" + UUID.randomUUID().toString().replace("-", "");
        new JdbcTemplate(ds).execute("create schema " + schema);
        ds.setUrl(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema);
        jdbc = new JdbcTemplate(ds); manager = new DataSourceTransactionManager(ds); tx = new TransactionTemplate(manager);
        jdbc.execute("create table payment_idempotency(scope varchar(100),request_key varchar(128),fingerprint varchar(64),provider_reference varchar(255),response text,primary key(scope,request_key))");
        jdbc.execute("create table customer_order_admission(customer_id bigint primary key,kind varchar(8),request_key varchar(128),fingerprint varchar(64),created_at timestamp default current_timestamp)");
        requests = mock(PaymentRequestService.class); transactions = mock(TransactionRepository.class); orders = mock(FinancialOrderClient.class);
        var locks = new PaymentLocks(jdbc);
        idempotency = new PaymentIdempotency(jdbc, locks, requests, transactions, manager);
        admission = new OrderAdmissionService(jdbc, locks, orders, transactions);
        when(orders.customerOrders(anyBoolean(),anyLong())).thenReturn(List.of());
        when(orders.attempt(anyBoolean(),anyLong(),anyString())).thenReturn(Map.of("state","UNKNOWN"));
        when(transactions.findByOrderId("ref")).thenReturn(Optional.of(Transaction.builder().id(1L).orderId("ref").status(TransactionStatus.PENDING).build()));
    }
    @AfterEach void cleanup() { if (jdbc != null) jdbc.execute("drop schema " + schema + " cascade"); }
    PaymentUrlResponse pay(String key, BigDecimal amount) { return idempotency.create(key,7L,"CUSTOMER","MOMO","FOOD_PAYMENT",1L,amount); }
    @Test void concurrentPaymentRetryReturnsSameResultAndCallsProviderOnce() throws Exception {
        var count = new AtomicInteger();
        when(requests.create(anyLong(),anyString(),anyString(),anyString(),anyLong(),any())).thenAnswer(call -> {
            count.incrementAndGet(); return PaymentUrlResponse.builder().orderId("ref").paymentUrl("https://pay.example/1").build();
        });
        var pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> calls = new ArrayList<>();
            for (int i=0;i<8;i++) calls.add(() -> pay(key,BigDecimal.TEN).getOrderId());
            for (var result : pool.invokeAll(calls)) assertEquals("ref",result.get());
            assertEquals(1,count.get());
            assertEquals(409,assertThrows(ResponseStatusException.class,()->pay(key,BigDecimal.ONE)).getStatusCode().value());
        } finally { pool.shutdownNow(); }
    }
    @Test void providerTimeoutRetainsReferenceAndRetryDoesNotSendAgain() {
        var independent = new TransactionTemplate(manager); independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        when(requests.create(anyLong(),anyString(),anyString(),anyString(),anyLong(),any())).thenAnswer(call -> {
            independent.executeWithoutResult(status -> PaymentIdempotency.bindProviderReference(jdbc,"ref"));
            throw new IllegalStateException("provider timeout");
        });
        assertThrows(IllegalStateException.class,()->pay(key,BigDecimal.TEN));
        assertEquals("ref",jdbc.queryForObject("select provider_reference from payment_idempotency",String.class));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->pay(key,BigDecimal.TEN)).getStatusCode().value());
        verify(requests,times(1)).create(anyLong(),anyString(),anyString(),anyString(),anyLong(),any());
    }
    @Test void failedProviderResultAllowsExplicitNewAttemptButTimeoutDoesNot() {
        jdbc.update("insert into payment_idempotency(scope,request_key,fingerprint,provider_reference) values ('CUSTOMER:7',?,'unused','ref')",key);
        // Populate the correct fingerprint through the ordinary failed request path instead.
        jdbc.update("delete from payment_idempotency");
        var independent = new TransactionTemplate(manager); independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        when(requests.create(anyLong(),anyString(),anyString(),anyString(),anyLong(),any())).thenAnswer(call -> {
            independent.executeWithoutResult(status -> PaymentIdempotency.bindProviderReference(jdbc,"ref")); throw new IllegalStateException();
        });
        assertThrows(IllegalStateException.class,()->pay(key,BigDecimal.TEN));
        when(transactions.findByOrderId("ref")).thenReturn(Optional.of(Transaction.builder().id(1L).status(TransactionStatus.FAILED).build()));
        assertEquals(422,assertThrows(ResponseStatusException.class,()->pay(key,BigDecimal.TEN)).getStatusCode().value());
    }
    @Test void simultaneousRideAndFoodDifferentKeysAllowOnlyOneReservation() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> calls = List.of(
                () -> reserve("RIDE", "ride-request-12345"), () -> reserve("FOOD", "food-request-12345"));
            int accepted = 0;
            for(var result : pool.invokeAll(calls)) if(result.get()) accepted++;
            assertEquals(1,accepted);
            assertEquals(1,jdbc.queryForObject("select count(*) from customer_order_admission",Integer.class));
        } finally { pool.shutdownNow(); }
    }
    boolean reserve(String kind,String requestKey) {
        try { tx.executeWithoutResult(status -> admission.reserve(7L,kind,requestKey,fingerprint)); return true; }
        catch(ResponseStatusException e) { assertEquals(409,e.getStatusCode().value()); return false; }
    }
    @Test void unknownReservationCanOnlyResumeOriginalKey() {
        assertTrue(reserve("FOOD",key)); assertTrue(reserve("FOOD",key));
        assertFalse(reserve("RIDE","other-request-12345"));
        when(orders.attempt(anyBoolean(),anyLong(),anyString())).thenReturn(Map.of("state","FAILED"));
        assertTrue(reserve("RIDE","other-request-12345"));
    }
    @Test void preExistingUnpaidOrderBlocksEvenWithoutReservationAndPaidOrderUnlocks() {
        when(orders.customerOrders(eq(false),anyLong())).thenReturn(List.of(Map.of("id",1L,"status","COMPLETED","paymentMethod","APP")));
        assertFalse(reserve("FOOD",key));
        when(transactions.findByBookingIdAndTransactionTypeOrderByIdDesc(anyLong(),any())).thenReturn(List.of(Transaction.builder().id(1L).status(TransactionStatus.SUCCESS).build()));
        assertTrue(reserve("FOOD",key));
    }
    @Test void sourceUnavailableFailsClosed() {
        when(orders.customerOrders(anyBoolean(),anyLong())).thenThrow(new IllegalStateException("offline"));
        assertThrows(IllegalStateException.class,()->reserve("FOOD",key));
        assertEquals(0,jdbc.queryForObject("select count(*) from customer_order_admission",Integer.class));
    }
}
