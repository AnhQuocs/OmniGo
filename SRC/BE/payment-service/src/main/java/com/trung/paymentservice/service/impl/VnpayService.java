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
import com.trung.paymentservice.service.PaymentWorkflowService;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnpayService implements PaymentStrategy {

    private final PaymentWorkflowService workflow;
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
            orderId = "VNPDEP_CUST_" + userId + "_" + UUID.randomUUID().toString();
            txType = TransactionType.DEPOSIT;
        } else if ("DEPOSIT".equalsIgnoreCase(type) || "DRIVER_DEPOSIT".equalsIgnoreCase(type)) {
            Wallet wallet = walletService.getOrCreateWallet(userId, UserType.DRIVER);
            walletId = wallet.getId();
            orderId = "VNPDEP_" + userId + "_" + UUID.randomUUID().toString();
            txType = TransactionType.DEPOSIT;
        } else if ("FOOD_PAYMENT".equalsIgnoreCase(type)) {
            Wallet wallet = walletService.getOrCreateWallet(userId, UserType.CUSTOMER);
            walletId = wallet.getId();
            orderId = "VNPFOOD_" + bookingId + "_" + UUID.randomUUID().toString();
            txType = TransactionType.FOOD_PAYMENT;
        } else {
            Wallet wallet = walletService.getOrCreateWallet(userId, UserType.CUSTOMER);
            walletId = wallet.getId();
            orderId = "VNPTRI_" + bookingId + "_" + UUID.randomUUID().toString();
            txType = TransactionType.TRIP_PAYMENT;
        }

        Transaction transaction = Transaction.builder()
                .driverId(driverId)
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

    public Map<String,String> processIpn(Map<String,String> params) {
        String signature = params.get("vnp_SecureHash");
        Map<String,String> signed = new TreeMap<>();
        params.forEach((key,value) -> {
            if (key.startsWith("vnp_") && !key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType") && value != null && !value.isEmpty()) signed.put(key,value);
        });
        String data = signed.entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));
        String expected = vnpayEncoder.hmacSHA512(vnpayConfig.getHashSecret(), data);
        if (signature == null || expected == null || expected.isBlank() || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII), signature.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII)))
            return Map.of("RspCode", "97", "Message", "Invalid signature");
        if (!Objects.equals(vnpayConfig.getTmnCode(), params.get("vnp_TmnCode"))) return Map.of("RspCode", "97", "Message", "Invalid merchant");
        BigDecimal amount;
        try { amount = new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal("100")); }
        catch (Exception e) { return Map.of("RspCode", "04", "Message", "Invalid amount"); }
        try {
            String txnStatus = params.get("vnp_TransactionStatus");
            boolean isSuccess = "00".equals(params.get("vnp_ResponseCode")) && (txnStatus == null || "00".equals(txnStatus));
            boolean applied = workflow.accept(params.get("vnp_TxnRef"), PaymentMethod.VNPAY, amount, params.get("vnp_TransactionNo"), isSuccess);
            return Map.of("RspCode", applied ? "00" : "02", "Message", applied ? "Confirm Success" : "Already confirmed");
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().value() == 404) return Map.of("RspCode", "01", "Message", "Order not found");
            if (e.getStatusCode().value() == 400) return Map.of("RspCode", "04", "Message", "Invalid payment data");
            throw e;
        }
    }
}
