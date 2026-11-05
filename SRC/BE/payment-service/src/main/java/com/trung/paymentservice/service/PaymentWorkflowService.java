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
import java.time.LocalDateTime;
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
    public void expire(String orderId, LocalDateTime cutoff) {
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
            boolean prepaid = Boolean.TRUE.equals(order.get("prepaid")) && tx.getPaymentMethod() == PaymentMethod.WALLET;
            if (prepaid && "AWAITING_PAYMENT".equals(order.get("status"))) {
                orders.markRidePaid(tx.getBookingId());
                order = orders.get(false, tx.getBookingId());
                if (FinancialOrderClient.cancelled(order)) {
                    refund(tx); tx.setFulfillmentDone(true); transactions.save(tx); return;
                }
            }
            if (prepaid && !"COMPLETED".equals(order.get("status"))) return;
            if (!"COMPLETED".equals(order.get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cuốc xe chưa hoàn thành");
            Long driverId = FinancialOrderClient.id(order, "driverId");
            if (driverId == null || (!prepaid && !Objects.equals(driverId, tx.getDriverId()))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Sai tài xế nhận tiền");
            BigDecimal gross = grossAmount(order);
            String incomeId = "RIDE_INCOME_" + tx.getBookingId();
            if (transactions.findByOrderId(incomeId).isEmpty()) {
                Wallet driver = walletService.creditWallet(driverId, UserType.DRIVER, gross.doubleValue());
                transactions.save(Transaction.builder().walletId(driver.getId()).bookingId(tx.getBookingId()).orderId(incomeId)
                        .amount(gross).transactionType(TransactionType.TRIP_INCOME).paymentMethod(PaymentMethod.WALLET)
                        .status(TransactionStatus.SUCCESS).fulfillmentDone(true).build());
                walletService.deductCommission(BookingCompletedEvent.builder().driverId(driverId).bookingId(tx.getBookingId()).amount(gross.doubleValue()).build());
            }
        }
        if (!food) recordVoucherExpense(false, tx.getBookingId(), order);
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
                    .foodAmount(grossAmount(order).subtract(FinancialOrderClient.money(order, "deliveryFee")).max(BigDecimal.ZERO))
                    .discountAmount(FinancialOrderClient.money(order, "discountAmount"))
                    .paymentMethod(String.valueOf(order.get("paymentMethod"))).build());
            recordVoucherExpense(true, id, order);
        } else if (Boolean.TRUE.equals(order.get("prepaid"))) {
            for (Transaction payment : payments) {
                if (payment.getStatus() == TransactionStatus.SUCCESS && transactions.findByOrderId("REFUND_PAYMENT_" + payment.getId()).isEmpty()) fulfill(payment.getId());
            }
        } else if (FinancialOrderClient.cash(order)) {
            creditCashVoucher(order, id);
            walletService.deductCommission(BookingCompletedEvent.builder().bookingId(id).driverId(FinancialOrderClient.id(order, "driverId"))
                    .amount(grossAmount(order).doubleValue()).build());
            recordVoucherExpense(false, id, order);
        }
    }
    // Platform-funded discounts are an expense, not another credit to recipient wallets.
    // Called under the order lock in the same transaction as the successful settlement.
    private void recordVoucherExpense(boolean food, Long id, Map<String,Object> order) {
        BigDecimal discount = FinancialOrderClient.money(order, "discountAmount");
        if (discount.signum() <= 0) return;
        String reference = "PLATFORM_VOUCHER_" + (food ? "FOOD_" : "RIDE_") + id;
        if (transactions.findByOrderId(reference).isPresent()) return;
        transactions.save(Transaction.builder().bookingId(id).orderId(reference)
                .amount(discount).transactionType(TransactionType.PLATFORM_VOUCHER_EXPENSE)
                .paymentMethod(PaymentMethod.WALLET).status(TransactionStatus.SUCCESS)
                .fulfillmentDone(true).build());
    }
    private BigDecimal grossAmount(Map<String,Object> order) {
        return FinancialOrderClient.money(order, order.get("originalAmount") == null ? "amount" : "originalAmount");
    }
    private void creditCashVoucher(Map<String,Object> order, Long id) {
        BigDecimal discount = FinancialOrderClient.money(order, "discountAmount");
        String reference = "RIDE_VOUCHER_" + id;
        if (discount.signum() <= 0 || transactions.findByOrderId(reference).isPresent()) return;
        Wallet driver = walletService.creditWallet(FinancialOrderClient.id(order, "driverId"), UserType.DRIVER, discount.doubleValue());
        transactions.save(Transaction.builder().walletId(driver.getId()).bookingId(id).orderId(reference)
                .amount(discount).transactionType(TransactionType.TRIP_INCOME).paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS).fulfillmentDone(true).build());
    }
    private void refund(Transaction payment) {
        String reference = "REFUND_PAYMENT_" + payment.getId();
        if (transactions.findByOrderId(reference).isPresent()) return;
        if (payment.getWalletId() == null) return;
        Wallet customer = wallets.findById(payment.getWalletId()).orElse(null);
        if (customer == null) return;
        if (payment.getAmount().signum() > 0) walletService.creditWallet(customer.getUserId(), customer.getUserType(), payment.getAmount().doubleValue());
        transactions.save(Transaction.builder().walletId(customer.getId()).bookingId(payment.getBookingId()).orderId(reference)
                .amount(payment.getAmount()).transactionType(TransactionType.REFUND).paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS).fulfillmentDone(true).build());
    }
}
