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
