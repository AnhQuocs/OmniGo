package com.trung.paymentservice.service;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.util.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
@Service @RequiredArgsConstructor
public class PendingPaymentStore {
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    private final WalletService walletService;
    private final TransactionRepository transactions;
    // Commit before contacting the provider. A timeout must not erase the provider's reference.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(Long userId, UserType userType, Long bookingId, Long driverId, String orderId,
                       String requestId, BigDecimal amount, TransactionType type) {
        var wallet = walletService.getOrCreateWallet(userId, userType);
        PaymentIdempotency.bindProviderReference(jdbc, orderId);
        transactions.save(Transaction.builder().walletId(wallet.getId()).bookingId(bookingId).driverId(driverId)
                .orderId(orderId).requestId(requestId).amount(amount).transactionType(type)
                .paymentMethod(PaymentMethod.MOMO).status(TransactionStatus.PENDING).fulfillmentDone(false).build());
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rejectCreation(String orderId) {
        // Conditional update serializes against IPN without acquiring the business lock held by the outer call.
        jdbc.update("update transactions set status='FAILED' where order_id=? and status='PENDING'", orderId);
    }
}
