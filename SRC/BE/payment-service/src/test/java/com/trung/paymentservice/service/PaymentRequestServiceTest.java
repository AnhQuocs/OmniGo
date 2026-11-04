package com.trung.paymentservice.service;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.strategy.PaymentFactory;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PaymentRequestServiceTest {
    private final FinancialOrderClient orders=mock(FinancialOrderClient.class);
    private final PaymentFactory factory=mock(PaymentFactory.class);
    private final PaymentRequestService service=new PaymentRequestService(mock(PaymentLocks.class),orders,mock(TransactionRepository.class),factory);
    private Map<String,Object> order() { return new HashMap<>(Map.of("customerId",1L,"amount",new BigDecimal("120000"),"paymentMethod","MOMO","status","AWAITING_PAYMENT")); }
    @Test void rejectsGrossChargeForDiscountedOrder() {
        var snapshot=order(); snapshot.put("amount",new BigDecimal("90000")); snapshot.put("originalAmount",new BigDecimal("120000"));
        when(orders.get(true,99L)).thenReturn(snapshot);
        assertThrows(ResponseStatusException.class,()->service.create(1L,"CUSTOMER","MOMO","FOOD_PAYMENT",99L,new BigDecimal("120000")));
        verifyNoInteractions(factory);
    }
    @Test void prepaidRideAllowsWalletBeforeDriverAssignmentButRejectsGateway() {
        var snapshot=order(); snapshot.put("paymentMethod","WALLET"); snapshot.put("prepaid",true);
        when(orders.get(false,99L)).thenReturn(snapshot);
        var repository=mock(TransactionRepository.class);
        var strategy=mock(com.trung.paymentservice.strategy.PaymentStrategy.class);
        when(factory.getStrategy("WALLET")).thenReturn(strategy);
        when(strategy.createPayment(1L,null,99L,new BigDecimal("120000"),"TRIP_PAYMENT"))
            .thenReturn(com.trung.paymentservice.dto.response.PaymentUrlResponse.builder().orderId("wallet-ref").paymentUrl("WALLET_SUCCESS").build());
        when(repository.findByOrderId("wallet-ref")).thenReturn(Optional.of(com.trung.paymentservice.entity.Transaction.builder().build()));
        var requests=new PaymentRequestService(mock(PaymentLocks.class),orders,repository,factory);
        assertEquals("WALLET_SUCCESS",requests.create(1L,"CUSTOMER","WALLET","TRIP_PAYMENT",99L,new BigDecimal("120000")).getPaymentUrl());
        verify(strategy).createPayment(1L,null,99L,new BigDecimal("120000"),"TRIP_PAYMENT");
        assertThrows(ResponseStatusException.class,()->requests.create(1L,"CUSTOMER","VNPAY","TRIP_PAYMENT",99L,new BigDecimal("120000")));
    }
    @Test void rejectsAnotherCustomersOrderAndClientSuppliedPrice() {
        when(orders.get(true,99L)).thenReturn(order());
        assertThrows(ResponseStatusException.class,()->service.create(2L,"CUSTOMER","MOMO","FOOD_PAYMENT",99L,new BigDecimal("120000")));
        assertThrows(ResponseStatusException.class,()->service.create(1L,"CUSTOMER","MOMO","FOOD_PAYMENT",99L,new BigDecimal("1")));
        verifyNoInteractions(factory);
    }
    @Test void rejectsCancelledOrdersCashAndUncompletedRides() {
        var order=order();when(orders.get(anyBoolean(),eq(99L))).thenReturn(order);
        order.put("status","CANCELLED");
        assertThrows(ResponseStatusException.class,()->service.create(1L,"CUSTOMER","MOMO","FOOD_PAYMENT",99L,new BigDecimal("120000")));
        order.put("status","AWAITING_PAYMENT");order.put("paymentMethod","CASH");
        assertThrows(ResponseStatusException.class,()->service.create(1L,"CUSTOMER","MOMO","FOOD_PAYMENT",99L,new BigDecimal("120000")));
        order.put("status","IN_PROGRESS");order.put("paymentMethod","APP");
        assertThrows(ResponseStatusException.class,()->service.create(1L,"CUSTOMER","MOMO","TRIP_PAYMENT",99L,new BigDecimal("120000")));
        verifyNoInteractions(factory);
    }
    @Test void rejectsWalletTopupAndWrongRole() {
        assertThrows(ResponseStatusException.class,()->service.create(1L,"CUSTOMER","WALLET","CUSTOMER_DEPOSIT",null,new BigDecimal("10000")));
        assertThrows(ResponseStatusException.class,()->service.create(1L,"CUSTOMER","MOMO","DRIVER_DEPOSIT",null,new BigDecimal("10000")));
        verifyNoInteractions(factory);
    }
}
