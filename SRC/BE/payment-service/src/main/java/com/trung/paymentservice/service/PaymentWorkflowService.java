package com.trung.paymentservice.service;
import com.trung.paymentservice.entity.*;
import com.trung.paymentservice.repository.*;
import com.trung.paymentservice.dto.request.*;
import com.trung.paymentservice.event.BookingCompletedEvent;
import com.trung.paymentservice.util.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import jakarta.persistence.EntityManager;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
@Service @RequiredArgsConstructor
public class PaymentWorkflowService {
    private final EntityManager entityManager;
    private final PaymentLocks locks;
    private final TransactionRepository transactions;
    private final WalletRepository wallets;
    private final WalletService walletService;
    private final FinancialOrderClient orders;
    public String key(Transaction tx) {
        return switch(tx.getTransactionType()) {
            case FOOD_PAYMENT -> "FOOD:" + tx.getBookingId();
            case TRIP_PAYMENT -> "RIDE:" + tx.getBookingId();
            default -> "PAYMENT:" + tx.getOrderId();
        };
    }
    @Transactional
    public void expire(String orderId, java.time.LocalDateTime cutoff) {
        Transaction initial = transactions.findByOrderId(orderId).orElseThrow();
        locks.lock(key(initial));
        Transaction tx = transactions.findByOrderIdForUpdate(orderId).orElseThrow();
        entityManager.refresh(tx);
        if (tx.getStatus() == TransactionStatus.PENDING && tx.getCreatedAt().isBefore(cutoff)) {
            tx.setStatus(TransactionStatus.CANCELED);
            transactions.save(tx);
        }
    }

    @Transactional
    public boolean accept(String orderId, PaymentMethod method, BigDecimal amount, String gatewayId, boolean successful) {
        Transaction initial = transactions.findByOrderId(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        locks.lock(key(initial));
        // Refresh after acquiring the business lock: another callback may have committed while we waited.
        Transaction tx = transactions.findByOrderIdForUpdate(orderId).orElseThrow();
        entityManager.refresh(tx);
        if (tx.getPaymentMethod() != method || tx.getAmount().compareTo(amount) != 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cổng hoặc số tiền không khớp");
        if (tx.getStatus() == TransactionStatus.SUCCESS) return false;
        if (!successful) {
            tx.setStatus(TransactionStatus.FAILED); transactions.save(tx); return true;
        }
        if (gatewayId == null || gatewayId.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu mã giao dịch cổng");
        tx.setStatus(TransactionStatus.SUCCESS); tx.setGatewayTransId(gatewayId);
        if (tx.getTransactionType() == TransactionType.DEPOSIT) {
            Wallet wallet = wallets.findById(tx.getWalletId()).orElseThrow();
            walletService.creditWallet(wallet.getUserId(), wallet.getUserType(), tx.getAmount().doubleValue());
            tx.setFulfillmentDone(true);
        } else {
            // A late payment for a second attempt is credited back once, never pays the order twice.
            boolean alreadyPaid = transactions.findByBookingIdAndTransactionTypeOrderByIdDesc(tx.getBookingId(), tx.getTransactionType()).stream()
                    .anyMatch(other -> !other.getId().equals(tx.getId()) && other.getStatus() == TransactionStatus.SUCCESS);
            if (alreadyPaid) { refund(tx); tx.setFulfillmentDone(true); }
            else tx.setFulfillmentDone(false);
        }
        transactions.save(tx); // Durable work item; fulfillment is retried independently of the provider callback.
        return true;
    }
    @Transactional
    public void fulfill(Long txId) {
        Transaction initial = transactions.findById(txId).orElseThrow();
        locks.lock(key(initial));
        Transaction tx = transactions.findByOrderIdForUpdate(initial.getOrderId()).orElseThrow();
        entityManager.refresh(tx);
        if (tx.isFulfillmentDone() || tx.getStatus() != TransactionStatus.SUCCESS) return;
        boolean food = tx.getTransactionType() == TransactionType.FOOD_PAYMENT;
        Map<String,Object> order = orders.get(food, tx.getBookingId());
        if (FinancialOrderClient.cancelled(order) || FinancialOrderClient.cash(order)) {
            refund(tx); tx.setFulfillmentDone(true); transactions.save(tx); return;
        }
        if (food) {
            orders.markFoodPaid(tx.getBookingId());
            order = orders.get(true, tx.getBookingId());
            if (FinancialOrderClient.cancelled(order) || FinancialOrderClient.cash(order)) refund(tx);
        } else {
            if (!"COMPLETED".equals(order.get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cuốc xe chưa hoàn thành");
            Long driverId = FinancialOrderClient.id(order, "driverId");
            if (driverId == null || !Objects.equals(driverId, tx.getDriverId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Sai tài xế nhận tiền");
            String incomeId = "RIDE_INCOME_" + tx.getBookingId();
            if (transactions.findByOrderId(incomeId).isEmpty()) {
                Wallet driver = walletService.creditWallet(driverId, UserType.DRIVER, tx.getAmount().doubleValue());
                transactions.save(Transaction.builder().walletId(driver.getId()).bookingId(tx.getBookingId()).orderId(incomeId)
                        .amount(tx.getAmount()).transactionType(TransactionType.TRIP_INCOME).paymentMethod(PaymentMethod.WALLET)
                        .status(TransactionStatus.SUCCESS).fulfillmentDone(true).build());
                walletService.deductCommission(BookingCompletedEvent.builder().driverId(driverId).bookingId(tx.getBookingId()).amount(tx.getAmount().doubleValue()).build());
            }
        }
        tx.setFulfillmentDone(true); transactions.save(tx);
    }
    @Transactional
    public void reconcile(boolean food, Long id) {
        locks.lock((food ? "FOOD:" : "RIDE:") + id);
        Map<String,Object> order = orders.get(food, id);
        var payments = transactions.findByBookingIdAndTransactionTypeOrderByIdDesc(id, food ? TransactionType.FOOD_PAYMENT : TransactionType.TRIP_PAYMENT);
        if (FinancialOrderClient.cancelled(order) || FinancialOrderClient.cash(order)) {
            for (Transaction tx : payments) if (tx.getStatus() == TransactionStatus.SUCCESS) refund(tx);
        }
        if (!"COMPLETED".equals(order.get("status"))) return;
        if (food) {
            if (!FinancialOrderClient.cash(order) && payments.stream().noneMatch(t -> t.getStatus() == TransactionStatus.SUCCESS && transactions.findByOrderId("REFUND_PAYMENT_" + t.getId()).isEmpty()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Chưa có thanh toán online hợp lệ");
            walletService.processFoodOrderPayout(FoodOrderPayoutRequest.builder().orderId(id)
                    .driverId(FinancialOrderClient.id(order, "driverId")).restaurantOwnerId(FinancialOrderClient.id(order, "restaurantOwnerId"))
                    .deliveryFee(FinancialOrderClient.money(order, "deliveryFee"))
                    .foodAmount(FinancialOrderClient.money(order, "amount").subtract(FinancialOrderClient.money(order, "deliveryFee")).max(BigDecimal.ZERO))
                    .paymentMethod(String.valueOf(order.get("paymentMethod"))).build());
        } else if (FinancialOrderClient.cash(order)) {
            walletService.deductCommission(BookingCompletedEvent.builder().bookingId(id).driverId(FinancialOrderClient.id(order, "driverId"))
                    .amount(FinancialOrderClient.money(order, "amount").doubleValue()).build());
        }
    }
    private void refund(Transaction payment) {
        String reference = "REFUND_PAYMENT_" + payment.getId();
        if (transactions.findByOrderId(reference).isPresent()) return;
        if (payment.getWalletId() == null) return;
        Wallet customer = wallets.findById(payment.getWalletId()).orElse(null);
        if (customer == null) return;
        walletService.creditWallet(customer.getUserId(), customer.getUserType(), payment.getAmount().doubleValue());
        transactions.save(Transaction.builder().walletId(customer.getId()).bookingId(payment.getBookingId()).orderId(reference)
                .amount(payment.getAmount()).transactionType(TransactionType.REFUND).paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS).fulfillmentDone(true).build());
    }
}
