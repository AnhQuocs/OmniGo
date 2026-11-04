package com.trung.paymentservice.service;

import com.trung.paymentservice.entity.*;
import com.trung.paymentservice.repository.*;
import com.trung.paymentservice.service.impl.WalletServiceImpl;
import com.trung.paymentservice.strategy.WalletPaymentStrategy;
import com.trung.paymentservice.util.enums.*;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(named = "TEST_POSTGRES_URL", matches = ".+")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentConcurrencyTest {
    @Configuration @EnableTransactionManagement(proxyTargetClass = true)
    @EnableJpaRepositories(basePackageClasses = WalletRepository.class)
    static class Config {
        @Bean DataSource dataSource() { return new DriverManagerDataSource(System.getenv("TEST_POSTGRES_URL"), "codex_test", ""); }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean(); factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.trung.paymentservice.entity"); factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop")); return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
        @Bean TransactionTemplate transactionTemplate(PlatformTransactionManager tm) { return new TransactionTemplate(tm); }
        @Bean PaymentLocks locks(DataSource ds) { return new PaymentLocks(new JdbcTemplate(ds)); }
        @Bean PlatformFeeService fees() { return mock(PlatformFeeService.class); }
        @Bean FinancialOrderClient orders() { return mock(FinancialOrderClient.class); }
        @Bean WalletServiceImpl wallets(EntityManagerFactory emf, PaymentLocks locks, PlatformFeeService fees, WalletRepository wallets, TransactionRepository transactions) {
            return new WalletServiceImpl(SharedEntityManagerCreator.createSharedEntityManager(emf), locks, fees, wallets, transactions, mock(RestTemplate.class));
        }
        @Bean PendingPaymentStore pending(DataSource ds, WalletService wallets, TransactionRepository transactions) { return new PendingPaymentStore(new JdbcTemplate(ds),wallets,transactions); }
        @Bean PaymentWorkflowService workflow(EntityManagerFactory emf, PaymentLocks locks, TransactionRepository txs, WalletRepository wallets, WalletService service, FinancialOrderClient orders) {
            return new PaymentWorkflowService(SharedEntityManagerCreator.createSharedEntityManager(emf), locks, txs, wallets, service, orders);
        }
        @Bean WalletPaymentStrategy walletPayments(WalletService service, WalletRepository wallets, TransactionRepository txs, PaymentLocks locks) {
            return new WalletPaymentStrategy(service, wallets, txs, locks);
        }
    }
    private AnnotationConfigApplicationContext context;
    private TransactionRepository txs;
    private WalletRepository wallets;
    private PaymentWorkflowService workflow;
    private FinancialOrderClient orders;
    private PlatformFeeService fees;
    @BeforeAll void start() {
        context = new AnnotationConfigApplicationContext(Config.class);
        txs=context.getBean(TransactionRepository.class); wallets=context.getBean(WalletRepository.class);
        workflow=context.getBean(PaymentWorkflowService.class); orders=context.getBean(FinancialOrderClient.class); fees=context.getBean(PlatformFeeService.class);
    }
    @AfterAll void stop() { if (context != null) context.close(); }
    @BeforeEach void resetData() {
        txs.deleteAll(); wallets.deleteAll(); reset(orders, fees);
        doReturn(new BigDecimal("20")).when(fees).getPercentage();
        when(fees.calculate(any(), any())).thenAnswer(i -> ((BigDecimal)i.getArgument(0)).multiply(i.getArgument(1)).divide(new BigDecimal("100"),0,java.math.RoundingMode.HALF_UP));
    }
    private Wallet wallet(long user, UserType type, String balance) {
        return wallets.save(Wallet.builder().userId(user).userType(type).balance(new BigDecimal(balance)).build());
    }
    private Transaction payment(Wallet wallet, String ref, TransactionType type, TransactionStatus status, String amount) {
        return txs.save(Transaction.builder().walletId(wallet.getId()).bookingId(99L).driverId(11L).orderId(ref).amount(new BigDecimal(amount))
                .transactionType(type).paymentMethod(PaymentMethod.MOMO).status(status).fulfillmentDone(false).build());
    }
    private Map<String,Object> order(String status, String method) {
        return new HashMap<>(Map.of("id",99L,"customerId",1L,"driverId",11L,"restaurantOwnerId",22L,"status",status,
                "paymentMethod",method,"amount",new BigDecimal("120000"),"deliveryFee",new BigDecimal("20000"),"paid",true));
    }
    private void parallel(Runnable action) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(8); CountDownLatch start=new CountDownLatch(1);
        try {
            List<Future<?>> futures=new ArrayList<>();
            for(int i=0;i<8;i++) futures.add(pool.submit(() -> { try { start.await(); action.run(); } catch(InterruptedException e){ throw new RuntimeException(e); } }));
            start.countDown(); for(Future<?> f:futures) f.get(25,TimeUnit.SECONDS);
        } finally { pool.shutdownNow(); }
    }
    private void balance(Long walletId, String amount) { assertEquals(0, wallets.findById(walletId).orElseThrow().getBalance().compareTo(new BigDecimal(amount))); }
    @Test void prepaidRideDebitsOnceBeforeDispatchAndPaysAssignedDriverOnlyOnCompletion() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"200000"), driver=wallet(11,UserType.DRIVER,"0");
        var strategy=context.getBean(WalletPaymentStrategy.class);
        parallel(() -> strategy.createPayment(1L,null,99L,new BigDecimal("120000"),"TRIP_PAYMENT"));
        balance(customer.getId(),"80000"); balance(driver.getId(),"0");
        Transaction payment=txs.findByBookingIdAndTransactionTypeOrderByIdDesc(99L,TransactionType.TRIP_PAYMENT).get(0);
        var state=order("AWAITING_PAYMENT","WALLET"); state.put("prepaid",true); state.put("driverId",null);
        when(orders.get(false,99L)).thenReturn(state);
        doAnswer(inv -> { state.put("status","PENDING"); state.put("paid",true); return null; }).when(orders).markRidePaid(99L);
        workflow.fulfill(payment.getId());
        verify(orders).markRidePaid(99L);
        assertFalse(txs.findById(payment.getId()).orElseThrow().isFulfillmentDone());
        state.put("status","IN_PROGRESS"); state.put("driverId",11L);
        workflow.fulfill(payment.getId()); balance(driver.getId(),"0");
        state.put("status","COMPLETED");
        parallel(() -> workflow.reconcile(false,99L));
        balance(customer.getId(),"80000"); balance(driver.getId(),"96000");
        assertTrue(txs.findById(payment.getId()).orElseThrow().isFulfillmentDone());
        assertEquals(1,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.TRIP_INCOME).count());
        assertEquals(1,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.COMMISSION_FEE).count());
    }
    @Test void prepaidRideCancellationRefundsOnceWithoutDriverIncome() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"200000");
        context.getBean(WalletPaymentStrategy.class).createPayment(1L,null,99L,new BigDecimal("120000"),"TRIP_PAYMENT");
        var state=order("CANCELLED","WALLET"); state.put("prepaid",true);
        when(orders.get(false,99L)).thenReturn(state);
        parallel(() -> workflow.reconcile(false,99L));
        balance(customer.getId(),"200000");
        assertEquals(1,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.REFUND).count());
        assertEquals(0,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.TRIP_INCOME).count());
    }
    @Test void insufficientPrepaidBalanceLeavesWalletAndLedgerUntouched() {
        Wallet customer=wallet(1,UserType.CUSTOMER,"10000");
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () ->
            context.getBean(WalletPaymentStrategy.class).createPayment(1L,null,99L,new BigDecimal("120000"),"TRIP_PAYMENT"));
        balance(customer.getId(),"10000"); assertEquals(0,txs.count());
        verifyNoInteractions(orders);
    }
    @Test void legacyTypeConstraintRollsBackOnlinePayoutUntilMigrationThenRetrySettlesOnce() throws Exception {
        var jdbc=new JdbcTemplate(context.getBean(DataSource.class));
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"), driver=wallet(11,UserType.DRIVER,"0"), restaurant=wallet(22,UserType.RESTAURANT,"0");
        payment(customer,"paid",TransactionType.FOOD_PAYMENT,TransactionStatus.SUCCESS,"120000");
        jdbc.execute("alter table transactions drop constraint if exists transactions_transaction_type_check");
        jdbc.execute("alter table transactions add constraint transactions_transaction_type_check check (transaction_type in ('DEPOSIT','TRIP_PAYMENT','TRIP_INCOME','COMMISSION_FEE','WITHDRAWAL','FOOD_PAYMENT','FOOD_DELIVERY_INCOME','REFUND'))");
        try {
            when(orders.get(true,99L)).thenReturn(order("COMPLETED","MOMO"));
            var failure=assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> workflow.reconcile(true,99L));
            assertTrue(failure.getMostSpecificCause().getMessage().contains("transactions_transaction_type_check"));
            balance(driver.getId(),"0"); balance(restaurant.getId(),"0");
            assertEquals(1,txs.count());
        } finally {
            // Apply the real upgrade to the legacy schema, not Hibernate's fresh schema.
            try (var input=new org.springframework.core.io.ClassPathResource("db/migration/V6__update_transaction_type_check.sql").getInputStream()) {
                String sql=new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
                context.getBean(TransactionTemplate.class).executeWithoutResult(status -> jdbc.execute(sql));
            }
        }
        parallel(() -> workflow.reconcile(true,99L));
        balance(driver.getId(),"16000"); balance(restaurant.getId(),"80000");
        assertEquals(5,txs.count());
        assertEquals(2,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.COMMISSION_FEE).count());
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("update transactions set transaction_type='INVALID' where order_id='paid'"));
    }
    @Test void vnpayFoodPaidBeforeDriverAssignmentSettlesAfterDelivery() throws Exception {
        foodPaidBeforeDriverAssignment(PaymentMethod.VNPAY);
    }
    @Test void walletFoodPaidBeforeDriverAssignmentSettlesAfterDelivery() throws Exception {
        foodPaidBeforeDriverAssignment(PaymentMethod.WALLET);
    }
    private void foodPaidBeforeDriverAssignment(PaymentMethod method) throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"200000"), driver=wallet(11,UserType.DRIVER,"0"), restaurant=wallet(22,UserType.RESTAURANT,"0");
        var awaiting=order("AWAITING_PAYMENT",method.name());
        awaiting.put("driverId",null);
        when(orders.get(true,99L)).thenReturn(awaiting);
        Transaction tx;
        if (method == PaymentMethod.WALLET) {
            var response=context.getBean(WalletPaymentStrategy.class).createPayment(1L,null,99L,new BigDecimal("120000"),"FOOD_PAYMENT");
            tx=txs.findByOrderId(response.getOrderId()).orElseThrow();
        } else {
            tx=payment(customer,"before-driver",TransactionType.FOOD_PAYMENT,TransactionStatus.PENDING,"120000");
            tx.setPaymentMethod(method); tx.setDriverId(null); tx=txs.save(tx);
            workflow.accept(tx.getOrderId(),method,new BigDecimal("120000"),"gateway-confirmed",true);
        }
        workflow.fulfill(tx.getId());
        assertTrue(txs.findById(tx.getId()).orElseThrow().isFulfillmentDone());
        balance(driver.getId(),"0"); balance(restaurant.getId(),"0");
        for (String status : List.of("PENDING","PREPARING","READY_FOR_PICKUP","DELIVERING")) {
            when(orders.get(true,99L)).thenReturn(order(status,method.name()));
            workflow.reconcile(true,99L);
            balance(driver.getId(),"0"); balance(restaurant.getId(),"0");
        }
        when(orders.get(true,99L)).thenReturn(order("COMPLETED",method.name()));
        parallel(() -> workflow.reconcile(true,99L));
        balance(driver.getId(),"16000"); balance(restaurant.getId(),"80000");
        assertEquals(2,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.COMMISSION_FEE).count());
        assertEquals(1,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.FOOD_DELIVERY_INCOME).count());
    }
    @Test void rejectedCreationSurvivesRollbackAndNeverOverwritesSuccessfulCallback() {
        var outer = context.getBean(TransactionTemplate.class);
        var store = context.getBean(PendingPaymentStore.class);
        store.create(1L,UserType.CUSTOMER,null,null,"rejected","request",new BigDecimal("100000"),TransactionType.DEPOSIT);
        assertThrows(IllegalStateException.class, () -> outer.executeWithoutResult(status -> {
            store.rejectCreation("rejected"); throw new IllegalStateException("outer rollback");
        }));
        assertEquals(TransactionStatus.FAILED,txs.findByOrderId("rejected").orElseThrow().getStatus());
        workflow.accept("rejected",PaymentMethod.MOMO,new BigDecimal("100000"),"gateway",true);
        store.rejectCreation("rejected");
        assertEquals(TransactionStatus.SUCCESS,txs.findByOrderId("rejected").orElseThrow().getStatus());
        balance(wallets.findByUserIdAndUserType(1L,UserType.CUSTOMER).orElseThrow().getId(),"100000");
    }
    @Test void providerTimeoutDoesNotErasePendingAttemptOrNewWallet() {
        var outer=context.getBean(TransactionTemplate.class);
        var store=context.getBean(PendingPaymentStore.class);
        assertThrows(IllegalStateException.class, () -> outer.executeWithoutResult(status -> {
            store.create(1L,UserType.CUSTOMER,null,null,"timeout","request",new BigDecimal("100000"),TransactionType.DEPOSIT);
            throw new IllegalStateException("provider response lost");
        }));
        assertEquals(TransactionStatus.PENDING,txs.findByOrderId("timeout").orElseThrow().getStatus());
        workflow.accept("timeout",PaymentMethod.MOMO,new BigDecimal("100000"),"g-timeout",true);
        balance(wallets.findByUserIdAndUserType(1L,UserType.CUSTOMER).orElseThrow().getId(),"100000");
    }
    @Test void distinctConcurrentDepositsNeverLoseWalletUpdates() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0");
        for(int i=0;i<8;i++) payment(customer,"deposit-"+i,TransactionType.DEPOSIT,TransactionStatus.PENDING,"100000");
        java.util.concurrent.atomic.AtomicInteger index=new java.util.concurrent.atomic.AtomicInteger();
        parallel(() -> { int i=index.getAndIncrement(); workflow.accept("deposit-"+i,PaymentMethod.MOMO,new BigDecimal("100000"),"g-"+i,true); });
        balance(customer.getId(),"800000");
    }
    @Test void signedWrongAmountOrWrongGatewayCannotCreditWallet() {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"); payment(customer,"deposit",TransactionType.DEPOSIT,TransactionStatus.PENDING,"100000");
        assertThrows(org.springframework.web.server.ResponseStatusException.class,() -> workflow.accept("deposit",PaymentMethod.MOMO,new BigDecimal("1"),"g",true));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,() -> workflow.accept("deposit",PaymentMethod.VNPAY,new BigDecimal("100000"),"g",true));
        balance(customer.getId(),"0"); assertEquals(TransactionStatus.PENDING,txs.findByOrderId("deposit").orElseThrow().getStatus());
    }
    @Test void lateSecondSuccessfulAttemptIsRefundedWithoutSecondPayout() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"), driver=wallet(11,UserType.DRIVER,"0"), restaurant=wallet(22,UserType.RESTAURANT,"0");
        payment(customer,"first",TransactionType.FOOD_PAYMENT,TransactionStatus.CANCELED,"120000");
        payment(customer,"second",TransactionType.FOOD_PAYMENT,TransactionStatus.PENDING,"120000");
        workflow.accept("second",PaymentMethod.MOMO,new BigDecimal("120000"),"g2",true);
        parallel(() -> workflow.accept("first",PaymentMethod.MOMO,new BigDecimal("120000"),"g1",true));
        when(orders.get(true,99L)).thenReturn(order("COMPLETED","MOMO"));
        parallel(() -> workflow.reconcile(true,99L));
        balance(customer.getId(),"120000"); balance(driver.getId(),"16000"); balance(restaurant.getId(),"80000");
    }
    @Test void onlineRidePaysNetOnceWhenFulfillmentIsRetried() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"), driver=wallet(11,UserType.DRIVER,"0");
        Transaction tx=payment(customer,"ride",TransactionType.TRIP_PAYMENT,TransactionStatus.SUCCESS,"120000");
        when(orders.get(false,99L)).thenReturn(order("COMPLETED","APP"));
        parallel(() -> workflow.fulfill(tx.getId())); balance(driver.getId(),"96000");
        assertEquals(1,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.COMMISSION_FEE).count());
    }
    @Test void expiringPendingCannotOverwriteConcurrentSuccessfulPayment() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0");payment(customer,"expiry",TransactionType.DEPOSIT,TransactionStatus.PENDING,"100000");
        parallel(() -> {
            workflow.expire("expiry",java.time.LocalDateTime.now().plusMinutes(1));
            workflow.accept("expiry",PaymentMethod.MOMO,new BigDecimal("100000"),"g",true);
        });
        balance(customer.getId(),"100000");assertEquals(TransactionStatus.SUCCESS,txs.findByOrderId("expiry").orElseThrow().getStatus());
    }
    @Test void simultaneousDepositCallbacksCreditExactlyOnce() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"); payment(customer,"deposit",TransactionType.DEPOSIT,TransactionStatus.PENDING,"100000");
        parallel(() -> workflow.accept("deposit",PaymentMethod.MOMO,new BigDecimal("100000"),"gateway-1",true));
        balance(customer.getId(),"100000");
        workflow.accept("deposit",PaymentMethod.MOMO,new BigDecimal("100000"),"gateway-1",false);
        assertEquals(TransactionStatus.SUCCESS,txs.findByOrderId("deposit").orElseThrow().getStatus());
    }
    @Test void repeatedWalletRequestsDebitOnlyOnce() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"200000");
        var strategy=context.getBean(WalletPaymentStrategy.class);
        parallel(() -> strategy.createPayment(1L,11L,99L,new BigDecimal("120000"),"FOOD_PAYMENT"));
        balance(customer.getId(),"80000"); assertEquals(1,txs.findByBookingId(99L).size());
    }
    @Test void simultaneousRefundRequestsRefundOriginalAmountOnlyOnce() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"); payment(customer,"paid",TransactionType.FOOD_PAYMENT,TransactionStatus.SUCCESS,"120000");
        when(orders.get(true,99L)).thenReturn(order("CANCELLED","MOMO"));
        parallel(() -> workflow.reconcile(true,99L)); balance(customer.getId(),"120000");
        assertEquals(1,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.REFUND).count());
    }
    @Test void failedRedisRollsBackBothPartiesThenRetryPaysExactlyOnce() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"), driver=wallet(11,UserType.DRIVER,"0"), restaurant=wallet(22,UserType.RESTAURANT,"0");
        payment(customer,"paid",TransactionType.FOOD_PAYMENT,TransactionStatus.SUCCESS,"120000");
        when(orders.get(true,99L)).thenReturn(order("COMPLETED","MOMO"));
        when(fees.getPercentage()).thenThrow(new IllegalStateException("Redis unavailable"));
        assertThrows(IllegalStateException.class,() -> workflow.reconcile(true,99L));
        balance(driver.getId(),"0"); balance(restaurant.getId(),"0"); assertEquals(1,txs.count());
        doReturn(new BigDecimal("20")).when(fees).getPercentage();
        parallel(() -> workflow.reconcile(true,99L));
        balance(driver.getId(),"16000"); balance(restaurant.getId(),"80000");
        assertEquals(2,txs.findAll().stream().filter(t -> t.getTransactionType()==TransactionType.COMMISSION_FEE).count());
    }
    @Test void cashNeverCreditsShippingAgain() throws Exception {
        Wallet driver=wallet(11,UserType.DRIVER,"0"), restaurant=wallet(22,UserType.RESTAURANT,"0");
        when(orders.get(true,99L)).thenReturn(order("COMPLETED","CASH"));
        parallel(() -> workflow.reconcile(true,99L));
        balance(driver.getId(),"-4000"); balance(restaurant.getId(),"-20000");
        assertEquals(2,txs.count());
    }
    @Test void remoteFailureLeavesDurableWorkAndRetrySucceeds() {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"); Transaction tx=payment(customer,"paid",TransactionType.FOOD_PAYMENT,TransactionStatus.SUCCESS,"120000");
        when(orders.get(true,99L)).thenReturn(order("AWAITING_PAYMENT","MOMO"));
        doThrow(new IllegalStateException("network down")).when(orders).markFoodPaid(99L);
        assertThrows(IllegalStateException.class,() -> workflow.fulfill(tx.getId()));
        assertFalse(txs.findById(tx.getId()).orElseThrow().isFulfillmentDone());
        doNothing().when(orders).markFoodPaid(99L); workflow.fulfill(tx.getId()); workflow.fulfill(tx.getId());
        assertTrue(txs.findById(tx.getId()).orElseThrow().isFulfillmentDone());
        verify(orders,times(2)).markFoodPaid(99L);
    }
    @Test void latePaymentAfterCashSwitchIsReturnedOnce() throws Exception {
        Wallet customer=wallet(1,UserType.CUSTOMER,"0"); Transaction tx=payment(customer,"late",TransactionType.FOOD_PAYMENT,TransactionStatus.CANCELED,"120000");
        when(orders.get(true,99L)).thenReturn(order("PENDING","CASH"));
        workflow.accept("late",PaymentMethod.MOMO,new BigDecimal("120000"),"g-late",true);
        parallel(() -> workflow.fulfill(tx.getId())); balance(customer.getId(),"120000");
    }
}
