package com.trung.paymentservice.strategy;
import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.entity.*;
import com.trung.paymentservice.repository.*;
import com.trung.paymentservice.service.*;
import com.trung.paymentservice.util.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class WalletPaymentStrategy implements PaymentStrategy {
    private final WalletService walletService;
    private final WalletRepository wallets;
    private final TransactionRepository transactions;
    private final PaymentLocks locks;
    public String getPaymentMethod() { return "WALLET"; }
    @Transactional
    public PaymentUrlResponse createPayment(Long userId, Long driverId, Long bookingId, BigDecimal amount, String type) {
        boolean food = "FOOD_PAYMENT".equals(type);
        if (bookingId == null || amount == null || amount.signum() <= 0 || (!food && !"TRIP_PAYMENT".equals(type))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        locks.lock((food ? "FOOD:" : "RIDE:") + bookingId);
        TransactionType txType = food ? TransactionType.FOOD_PAYMENT : TransactionType.TRIP_PAYMENT;
        for (Transaction tx : transactions.findByBookingIdAndTransactionTypeOrderByIdDesc(bookingId, txType)) {
            if (tx.getStatus() == TransactionStatus.SUCCESS) return PaymentUrlResponse.builder().orderId(tx.getOrderId()).paymentUrl("WALLET_SUCCESS").build();
            if (tx.getStatus() == TransactionStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT, "Giao dịch đang xử lý");
        }
        walletService.getOrCreateWallet(userId, UserType.CUSTOMER);
        Wallet wallet = wallets.findByUserIdAndUserTypeWithLock(userId, UserType.CUSTOMER).orElseThrow();
        if (wallet.getBalance().compareTo(amount) < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số dư ví không đủ");
        wallet.setBalance(wallet.getBalance().subtract(amount)); wallets.save(wallet);
        String ref = "WAL_" + UUID.randomUUID();
        transactions.save(Transaction.builder().walletId(wallet.getId()).bookingId(bookingId).driverId(driverId).orderId(ref)
                .amount(amount).transactionType(txType).paymentMethod(PaymentMethod.WALLET).status(TransactionStatus.SUCCESS)
                .fulfillmentDone(false).build());
        return PaymentUrlResponse.builder().orderId(ref).paymentUrl("WALLET_SUCCESS").build();
    }
}
