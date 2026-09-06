package com.trung.paymentservice.strategy;

import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.entity.Wallet;
import com.trung.paymentservice.event.BookingCompletedEvent;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.repository.WalletRepository;
import com.trung.paymentservice.service.WalletService;
import com.trung.paymentservice.util.enums.PaymentMethod;
import com.trung.paymentservice.util.enums.TransactionStatus;
import com.trung.paymentservice.util.enums.TransactionType;
import com.trung.paymentservice.util.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletPaymentStrategy implements PaymentStrategy {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final RestTemplate restTemplate;

    @Value("${app.food-delivery-url:http://localhost:8086}")
    private String foodDeliveryBaseUrl;

    @Override
    public String getPaymentMethod() {
        return "WALLET";
    }

    @Override
    @Transactional
    public PaymentUrlResponse createPayment(Long userId, Long driverId, Long bookingId, BigDecimal amount, String type) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền thanh toán phải lớn hơn 0!");
        }

        Wallet customerWallet = walletRepository.findByUserIdAndUserTypeWithLock(userId, UserType.CUSTOMER)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .userId(userId)
                        .userType(UserType.CUSTOMER)
                        .balance(BigDecimal.ZERO)
                        .build()));

        if (customerWallet.getBalance().compareTo(amount) < 0) {
            String currentBalanceStr = NumberUtilsFormat(customerWallet.getBalance());
            String requiredAmountStr = NumberUtilsFormat(amount);
            log.warn("Khách hàng {} không đủ số dư ví để thanh toán: Có {} đ, Cần {} đ", userId, currentBalanceStr, requiredAmountStr);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số dư ví không đủ (Hiện có: " + currentBalanceStr + " đ, Cần: " + requiredAmountStr + " đ). Vui lòng nạp thêm tiền vào ví!"
            );
        }

        // Khấu trừ số dư ví khách hàng
        customerWallet.setBalance(customerWallet.getBalance().subtract(amount));
        walletRepository.save(customerWallet);

        String orderId;
        TransactionType txType;

        if ("FOOD_PAYMENT".equalsIgnoreCase(type)) {
            orderId = "WALFOOD_" + bookingId + "_" + System.currentTimeMillis();
            txType = TransactionType.FOOD_PAYMENT;

            Transaction transaction = Transaction.builder()
                    .walletId(customerWallet.getId())
                    .bookingId(bookingId)
                    .orderId(orderId)
                    .amount(amount)
                    .transactionType(txType)
                    .paymentMethod(PaymentMethod.WALLET)
                    .status(TransactionStatus.SUCCESS)
                    .build();
            transactionRepository.save(transaction);

            log.info("Khách hàng {} thanh toán đơn đồ ăn #{} bằng ví thành công: -{} đ. Số dư còn: {} đ",
                    userId, bookingId, amount, customerWallet.getBalance());

            // Thông báo sang food-delivery-service
            notifyFoodOrderPaid(bookingId);

        } else {
            // TRIP_PAYMENT
            orderId = "WALTRI_" + bookingId + "_" + System.currentTimeMillis();
            txType = TransactionType.TRIP_PAYMENT;

            Transaction transaction = Transaction.builder()
                    .walletId(customerWallet.getId())
                    .bookingId(bookingId)
                    .orderId(orderId)
                    .amount(amount)
                    .transactionType(txType)
                    .paymentMethod(PaymentMethod.WALLET)
                    .status(TransactionStatus.SUCCESS)
                    .build();
            transactionRepository.save(transaction);

            log.info("Khách hàng {} thanh toán cuốc xe #{} bằng ví thành công: -{} đ. Số dư còn: {} đ",
                    userId, bookingId, amount, customerWallet.getBalance());

            // Nếu có driverId, cộng tiền và trừ hoa hồng cho tài xế
            if (driverId != null) {
                creditDriverForTrip(driverId, transaction);
            }
        }

        return PaymentUrlResponse.builder()
                .orderId(orderId)
                .paymentUrl("WALLET_SUCCESS")
                .build();
    }

    private void notifyFoodOrderPaid(Long orderId) {
        if (orderId == null) return;
        try {
            String url = (foodDeliveryBaseUrl != null ? foodDeliveryBaseUrl : "http://localhost:8086") + "/api/v1/food-orders/" + orderId + "/paid";
            restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
            log.info("Đã thông báo sang food-delivery-service: Đơn hàng #{} đã thanh toán bằng ví thành công", orderId);
        } catch (Exception e) {
            log.error("Lỗi thông báo cập nhật thanh toán đơn đồ ăn #{}: {}", orderId, e.getMessage());
        }
    }

    private void creditDriverForTrip(Long driverId, Transaction customerTransaction) {
        if (driverId == null) return;
        Wallet driverWallet = walletService.getOrCreateWallet(driverId, UserType.DRIVER);
        walletService.creditWallet(driverId, UserType.DRIVER, customerTransaction.getAmount().doubleValue());

        Transaction driverIncomeTx = Transaction.builder()
                .walletId(driverWallet.getId())
                .bookingId(customerTransaction.getBookingId())
                .orderId("INC_" + customerTransaction.getOrderId())
                .amount(customerTransaction.getAmount())
                .transactionType(TransactionType.TRIP_INCOME)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(driverIncomeTx);

        BookingCompletedEvent event = BookingCompletedEvent.builder()
                .bookingId(customerTransaction.getBookingId())
                .driverId(driverId)
                .amount(customerTransaction.getAmount().doubleValue())
                .build();

        walletService.deductCommission(event);
    }

    private String NumberUtilsFormat(BigDecimal val) {
        if (val == null) return "0";
        return String.format("%,.0f", val.doubleValue());
    }
}
