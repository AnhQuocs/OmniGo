package com.trung.paymentservice.service;
import com.trung.paymentservice.controller.PaymentController;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.util.enums.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PlatformVoucherStatsTest {
    @Mock TransactionRepository transactions;
    @InjectMocks PaymentController controller;
    @Test void platformBearsDiscountEvenWhenItExceedsCommission() {
        when(transactions.findAll()).thenReturn(List.of(
            entry(TransactionType.COMMISSION_FEE, "24000", TransactionStatus.SUCCESS),
            entry(TransactionType.PLATFORM_VOUCHER_EXPENSE, "30000", TransactionStatus.SUCCESS),
            entry(TransactionType.PLATFORM_VOUCHER_EXPENSE, "50000", TransactionStatus.FAILED)));
        var stats = controller.getAdminPaymentStats().getBody();
        assertEquals(new BigDecimal("24000"), stats.get("commissionRevenue"));
        assertEquals(new BigDecimal("30000"), stats.get("voucherExpense"));
        assertEquals(new BigDecimal("-6000"), stats.get("netRevenue"));
        assertEquals(new BigDecimal("24000"), stats.get("totalVolume"));
    }
    private Transaction entry(TransactionType type, String amount, TransactionStatus status) {
        return Transaction.builder().transactionType(type).amount(new BigDecimal(amount)).status(status).build();
    }
}
