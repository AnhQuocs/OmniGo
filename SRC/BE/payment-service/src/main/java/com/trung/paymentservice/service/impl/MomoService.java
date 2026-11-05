package com.trung.paymentservice.service.impl;

import com.trung.paymentservice.config.MomoConfig;
import com.trung.paymentservice.dto.request.MomoCreateRequest;
import com.trung.paymentservice.dto.request.MomoIpnRequest;
import com.trung.paymentservice.dto.response.MomoCreateResponse;
import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.entity.Wallet;
import com.trung.paymentservice.event.BookingCompletedEvent;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.repository.WalletRepository;
import com.trung.paymentservice.service.MomoEncoder;
import com.trung.paymentservice.service.PaymentWorkflowService;
import com.trung.paymentservice.service.WalletService;
import com.trung.paymentservice.strategy.PaymentStrategy;
import com.trung.paymentservice.util.enums.PaymentMethod;
import com.trung.paymentservice.util.enums.TransactionStatus;
import com.trung.paymentservice.util.enums.TransactionType;
import com.trung.paymentservice.util.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.net.URI;
import java.util.UUID;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trung.paymentservice.service.PendingPaymentStore;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService implements PaymentStrategy {

    private final PendingPaymentStore pendingPayments;
    private final PaymentWorkflowService workflow;
    private final MomoConfig momoConfig;
    private final MomoEncoder momoEncoder;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final RestTemplate restTemplate;

    @Value ("${app.food-delivery-url:http://localhost:8086}")
    private String foodDeliveryBaseUrl;

    @Override
    public String getPaymentMethod() {
        return "MOMO";
    }

    private String getRequestType() {
        return (momoConfig.getRequestType() != null && !momoConfig.getRequestType().isBlank())
                ? momoConfig.getRequestType()
                : "captureWallet";
    }

    @Override
    @Transactional
    public PaymentUrlResponse createPayment(Long userId, Long driverId, Long bookingId, BigDecimal amount, String type) {
        MomoCreateResponse momoResponse;
        String orderId;
        String requestType = getRequestType();

        if ("CUSTOMER_DEPOSIT".equalsIgnoreCase(type)) {
            orderId = "DEPOSIT_CUST_" + userId + "_" + UUID.randomUUID().toString();
            String requestId = UUID.randomUUID().toString();
            String extraData = "USER_TYPE=CUSTOMER;USER_ID=" + userId;

            pendingPayments.create(userId, UserType.CUSTOMER, null, null, orderId, requestId, amount, TransactionType.DEPOSIT);
            momoResponse = executeMomoApi(orderId, requestId, amount, "Nap tien vi khach hang #" + userId, requestType, extraData);
        } else if ("DEPOSIT".equalsIgnoreCase(type) || "DRIVER_DEPOSIT".equalsIgnoreCase(type)) {
            orderId = "DEPOSIT_" + userId + "_" + UUID.randomUUID().toString();
            String requestId = UUID.randomUUID().toString();
            String extraData = "USER_TYPE=DRIVER;USER_ID=" + userId;

            pendingPayments.create(userId, UserType.DRIVER, null, null, orderId, requestId, amount, TransactionType.DEPOSIT);
            momoResponse = executeMomoApi(orderId, requestId, amount, "Nap tien vi tai xe #" + userId, requestType, extraData);
        } else if ("FOOD_PAYMENT".equalsIgnoreCase(type)) {
            orderId = "FOOD_" + bookingId + "_" + UUID.randomUUID().toString();
            String requestId = UUID.randomUUID().toString();
            String extraData = "BOOKING_ID=" + bookingId + ";TYPE=FOOD_PAYMENT";

            pendingPayments.create(userId, UserType.CUSTOMER, bookingId, driverId, orderId, requestId, amount, TransactionType.FOOD_PAYMENT);
            momoResponse = executeMomoApi(orderId, requestId, amount, "Thanh toan don mon #" + bookingId, requestType, extraData);
        } else {
            orderId = "TRIP_" + bookingId + "_" + UUID.randomUUID().toString();
            String requestId = UUID.randomUUID().toString();
            String extraData = "BOOKING_ID=" + bookingId + ";DRIVER_ID=" + driverId;

            pendingPayments.create(userId, UserType.CUSTOMER, bookingId, driverId, orderId, requestId, amount, TransactionType.TRIP_PAYMENT);
            momoResponse = executeMomoApi(orderId, requestId, amount, "Thanh toan cuoc xe #" + bookingId, requestType, extraData);
        }

        // Only explicit terminal rejections are safe to retry. Duplicate/processing/unknown codes stay pending.
        if (momoResponse != null && momoResponse.getResultCode() != null
                && Set.of(20, 21, 22, 1001, 1006).contains(momoResponse.getResultCode())
                && Objects.equals(orderId, momoResponse.getOrderId())
                && Objects.equals(momoConfig.getPartnerCode(), momoResponse.getPartnerCode())) {
            pendingPayments.rejectCreation(orderId);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "MoMo đã từ chối khởi tạo giao dịch. Vui lòng kiểm tra thông tin rồi thử lại.");
        }
        if (momoResponse == null || !Integer.valueOf(0).equals(momoResponse.getResultCode()) || momoResponse.getPayUrl() == null || momoResponse.getPayUrl().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không tạo được giao dịch MoMo");
        return PaymentUrlResponse.builder()
                .orderId(orderId)
                .paymentUrl(momoResponse != null ? momoResponse.getPayUrl() : null)
                .build();
    }

    public void processMomoIpn(MomoIpnRequest request) {
        if (!verifyIpnSignature(request) || !Objects.equals(momoConfig.getPartnerCode(), request.getPartnerCode()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature or merchant");
        Transaction tx = transactionRepository.findByOrderId(request.getOrderId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (request.getAmount() == null || (tx.getRequestId() != null && !tx.getRequestId().equals(request.getRequestId())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        workflow.accept(request.getOrderId(), PaymentMethod.MOMO, BigDecimal.valueOf(request.getAmount()), request.getTransId(), Integer.valueOf(0).equals(request.getResultCode()));
    }

    private boolean verifyIpnSignature(MomoIpnRequest req) {
        try {
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() + "&amount=" + req.getAmount()
                    + "&extraData=" + req.getExtraData() + "&message=" + req.getMessage()
                    + "&orderId=" + req.getOrderId() + "&orderInfo=" + req.getOrderInfo()
                    + "&orderType=" + req.getOrderType() + "&partnerCode=" + req.getPartnerCode()
                    + "&payType=" + req.getPayType() + "&requestId=" + req.getRequestId()
                    + "&responseTime=" + req.getResponseTime() + "&resultCode=" + req.getResultCode()
                    + "&transId=" + req.getTransId();
            String expected = momoEncoder.signHmacSHA256(rawSignature, momoConfig.getSecretKey());
            return req.getSignature() != null && expected != null && !expected.isBlank()
                    && MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), req.getSignature().getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) {
            return false;
        }
    }

    private Long parseDataFromExtra(String extraData, String key) {
        if (extraData == null || extraData.isEmpty()) return null;
        try {
            for (String param : extraData.split(";")) {
                if (param.startsWith(key)) return Long.parseLong(param.split("=")[1]);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String parseDataFromExtraStr(String extraData, String key) {
        if (extraData == null || extraData.isEmpty()) return null;
        try {
            for (String param : extraData.split(";")) {
                if (param.startsWith(key)) return param.split("=")[1];
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private MomoCreateResponse executeMomoApi(String orderId, String requestId, BigDecimal amount, String orderInfo, String requestType, String extraData) {
        try {
            String amountStr = String.valueOf(amount.setScale(0, RoundingMode.HALF_UP).longValue());
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() + "&amount=" + amountStr
                    + "&extraData=" + extraData + "&ipnUrl=" + momoConfig.getIpnUrl() + "&orderId=" + orderId
                    + "&orderInfo=" + orderInfo + "&partnerCode=" + momoConfig.getPartnerCode()
                    + "&redirectUrl=" + momoConfig.getRedirectUrl() + "&requestId=" + requestId + "&requestType=" + requestType;
            String signature = momoEncoder.signHmacSHA256(rawSignature, momoConfig.getSecretKey());

            MomoCreateRequest request = MomoCreateRequest.builder().partnerCode(momoConfig.getPartnerCode())
                    .requestId(requestId).amount(amount.longValue()).orderId(orderId).orderInfo(orderInfo)
                    .redirectUrl(momoConfig.getRedirectUrl()).ipnUrl(momoConfig.getIpnUrl()).requestType(requestType)
                    .extraData(extraData).lang("vi").signature(signature).build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MomoCreateRequest> entity = new HttpEntity<>(request, headers);

            // Gọi API MoMo
            URI targetUrl = URI.create(momoConfig.getEndpoint());

            return restTemplate.postForEntity(targetUrl, entity, MomoCreateResponse.class).getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("MoMo API báo lỗi HTTP Status: {}, Response Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            // A structured rejection can arrive with HTTP 4xx. Never interpret a bare HTTP error as a final result.
            try {
                var response = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(ex.getResponseBodyAsString(), MomoCreateResponse.class);
                if (Objects.equals(orderId, response.getOrderId()) && Objects.equals(requestId, response.getRequestId())
                        && Objects.equals(momoConfig.getPartnerCode(), response.getPartnerCode())) return response;
            } catch (Exception ignored) { /* Keep the independently persisted pending attempt. */ }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Chưa xác định kết quả khởi tạo MoMo; giữ giao dịch cũ để kiểm tra");
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi gọi MoMo: ", e);
            throw new RuntimeException("Lỗi kết nối MoMo do hệ thống nội bộ");
        }
    }
}