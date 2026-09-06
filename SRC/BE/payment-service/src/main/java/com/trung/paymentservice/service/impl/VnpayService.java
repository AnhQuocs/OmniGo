package com.trung.paymentservice.service.impl;

import com.trung.paymentservice.config.VnpayConfig;
import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.entity.Wallet;
import com.trung.paymentservice.event.BookingCompletedEvent;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.repository.WalletRepository;
import com.trung.paymentservice.service.VnpayEncoder;
import com.trung.paymentservice.service.WalletService;
import com.trung.paymentservice.strategy.PaymentStrategy;
import com.trung.paymentservice.util.enums.PaymentMethod;
import com.trung.paymentservice.util.enums.TransactionStatus;
import com.trung.paymentservice.util.enums.TransactionType;
import com.trung.paymentservice.util.enums.UserType;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayService implements PaymentStrategy {

    private final VnpayConfig vnpayConfig;
    private final VnpayEncoder vnpayEncoder;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final RestTemplate restTemplate;

    @Value ("${app.food-delivery-url:http://localhost:8086}")
    private String foodDeliveryBaseUrl;

    @Override
    public String getPaymentMethod() {
        return "VNPAY";
    }

    @Override
    @Transactional
    public PaymentUrlResponse createPayment(Long userId, Long driverId, Long bookingId, BigDecimal amount, String type) {
        String orderId;
        Long walletId;

        TransactionType txType;
        if ("CUSTOMER_DEPOSIT".equalsIgnoreCase(type)) {
            Wallet wallet = walletService.getOrCreateWallet(userId, UserType.CUSTOMER);
            walletId = wallet.getId();
            orderId = "VNPDEP_CUST_" + userId + "_" + System.currentTimeMillis();
            txType = TransactionType.DEPOSIT;
        } else if ("DEPOSIT".equalsIgnoreCase(type) || "DRIVER_DEPOSIT".equalsIgnoreCase(type)) {
            Wallet wallet = walletService.getOrCreateWallet(userId, UserType.DRIVER);
            walletId = wallet.getId();
            orderId = "VNPDEP_" + userId + "_" + System.currentTimeMillis();
            txType = TransactionType.DEPOSIT;
        } else if ("FOOD_PAYMENT".equalsIgnoreCase(type)) {
            Wallet wallet = walletService.getOrCreateWallet(userId, UserType.CUSTOMER);
            walletId = wallet.getId();
            orderId = "VNPFOOD_" + bookingId + "_" + System.currentTimeMillis();
            txType = TransactionType.FOOD_PAYMENT;
        } else {
            Wallet wallet = walletService.getOrCreateWallet(userId, UserType.CUSTOMER);
            walletId = wallet.getId();
            orderId = "VNPTRI_" + bookingId + "_" + System.currentTimeMillis();
            txType = TransactionType.TRIP_PAYMENT;
        }

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .bookingId(bookingId)
                .orderId(orderId)
                .amount(amount)
                .transactionType(txType)
                .paymentMethod(PaymentMethod.valueOf("VNPAY"))
                .status(TransactionStatus.PENDING)
                .build();
        transactionRepository.save(transaction);

        // Chuẩn bị tham số gửi sang VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount.multiply(new BigDecimal("100")).longValue())); // VNPay tính bằng đơn vị xu
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));

        vnpParams.put("vnp_OrderInfo", "BK=" + bookingId + ";DR=" + driverId + ";TY=" + type);

        String paymentUrl = vnpayEncoder.getPaymentUrl(vnpParams, vnpayConfig.getHashSecret(), vnpayConfig.getEndpoint());

        return PaymentUrlResponse.builder().orderId(orderId).paymentUrl(paymentUrl).build();
    }

    @Transactional
    public void processVnpayReturn(String orderId, String responseCode, String orderInfo) {
        Transaction transaction = transactionRepository.findByOrderId(orderId).orElse(null);
        if (transaction == null || transaction.getStatus() == TransactionStatus.SUCCESS) return;

        if ("00".equals(responseCode)) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setGatewayTransId("VNP_" + System.currentTimeMillis());
            transactionRepository.save(transaction);

            Wallet wallet = walletRepository.findById(transaction.getWalletId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví tương ứng với giao dịch"));

            if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
                walletService.creditWallet(wallet.getUserId(), wallet.getUserType(), transaction.getAmount().doubleValue());
            } else if (transaction.getTransactionType() == TransactionType.FOOD_PAYMENT) {
                notifyFoodOrderPaid(transaction.getBookingId());
            } else {
                Long driverId = parseData(orderInfo, "DR=");
                creditDriverForTrip(driverId, transaction);
            }
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
        }
    }

    private void notifyFoodOrderPaid(Long orderId) {
        if (orderId == null) return;
        try {
            String url = (foodDeliveryBaseUrl != null ? foodDeliveryBaseUrl : "http://localhost:8086") + "/api/v1/food-orders/" + orderId + "/paid";
            restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
            org.slf4j.LoggerFactory.getLogger(VnpayService.class).info("Đã thông báo sang food-delivery-service ({}/api/v1/food-orders/{}/paid): Đơn hàng #{} đã thanh toán thành công qua VNPay", foodDeliveryBaseUrl, orderId, orderId);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(VnpayService.class).error("Lỗi thông báo cập nhật thanh toán đơn đồ ăn #{}: {}", orderId, e.getMessage());
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

    private Long parseData(String orderInfo, String key) {
        try {
            for (String param : orderInfo.split(";")) {
                if (param.startsWith(key)) return Long.parseLong(param.split("=")[1]);
            }
        } catch (Exception ignored) {}
        return null;
    }
}