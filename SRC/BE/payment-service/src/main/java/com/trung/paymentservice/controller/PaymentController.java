package com.trung.paymentservice.controller;

import com.trung.paymentservice.service.FinancialOrderClient;
import com.trung.paymentservice.dto.request.FoodOrderPayoutRequest;
import com.trung.paymentservice.dto.request.MomoIpnRequest;
import com.trung.paymentservice.dto.request.RefundRequest;
import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.dto.response.WalletResponse;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.entity.Wallet;
import com.trung.paymentservice.mapper.PaymentMapper;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.repository.WalletRepository;
import com.trung.paymentservice.service.WalletService;
import com.trung.paymentservice.service.impl.MomoService;
import com.trung.paymentservice.service.impl.VnpayService;
import com.trung.paymentservice.strategy.PaymentFactory;
import com.trung.paymentservice.strategy.PaymentStrategy;
import com.trung.paymentservice.util.enums.TransactionStatus;
import com.trung.paymentservice.util.enums.TransactionType;
import com.trung.paymentservice.util.enums.UserType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trung.paymentservice.service.PaymentRequestService;
import com.trung.paymentservice.service.PaymentWorkflowService;
import com.trung.paymentservice.service.PlatformFeeService;
import com.trung.paymentservice.util.enums.PaymentMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final com.trung.paymentservice.service.OrderAdmissionService admission;
    public record AdmissionRequest(Long customerId, String kind, String key, String fingerprint) {}
    @PostMapping("/internal/order-admission")
    public ResponseEntity<Void> reserveOrder(@RequestBody AdmissionRequest request) {
        admission.reserve(request.customerId(), request.kind(), request.key(), request.fingerprint());
        return ResponseEntity.noContent().build();
    }

    private final PlatformFeeService platformFeeService;
    private final PaymentRequestService paymentRequests;
    private final com.trung.paymentservice.service.PaymentIdempotency paymentIdempotency;
    private final PaymentWorkflowService workflow;

    private final FinancialOrderClient financialOrders;
    @PostMapping("/internal/rides/{id}/prepay")
    public ResponseEntity<Void> prepayRide(@PathVariable Long id) {
        var order = financialOrders.get(false, id);
        if (!Boolean.TRUE.equals(order.get("prepaid")) || !"WALLET".equals(order.get("paymentMethod")))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chuyến không dùng ví trả trước");
        var result = paymentRequests.create(FinancialOrderClient.id(order, "customerId"),
                "CUSTOMER", "WALLET", "TRIP_PAYMENT", id, FinancialOrderClient.money(order, "amount"));
        var transaction = transactionRepository.findByOrderId(result.getOrderId()).orElseThrow();
        workflow.fulfill(transaction.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/internal/reconcile/{kind}/{id}")
    public ResponseEntity<Void> reconcile(@PathVariable String kind, @PathVariable Long id) {
        if (!Set.of("food", "ride").contains(kind)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        workflow.reconcile("food".equals(kind), id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/platform-fee")
    public Map<String, BigDecimal> getPublicPlatformFee() {
        return Map.of("percentage", platformFeeService.getPercentage());
    }

    @GetMapping("/admin/platform-fee")
    public Map<String, BigDecimal> getPlatformFee() {
        return Map.of("percentage", platformFeeService.getPercentage());
    }

    @PutMapping("/admin/platform-fee")
    public Map<String, BigDecimal> updatePlatformFee(@RequestBody Map<String, BigDecimal> request) {
        return Map.of("percentage", platformFeeService.update(request.get("percentage")));
    }

    private final WalletService walletService;
    private final PaymentMapper paymentMapper;
    private final MomoService momoService;
    private final VnpayService vnpayService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final PaymentFactory paymentFactory;

    @PostMapping("/internal/drivers/{driverId}/booking-fees")
    public BigDecimal getBookingFees(@PathVariable Long driverId, @RequestBody List<Long> bookingIds) {
        return walletRepository.findByUserIdAndUserType(driverId, UserType.DRIVER)
                .map(wallet -> transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream()
                        .filter(t -> t.getStatus() == TransactionStatus.SUCCESS
                                && t.getTransactionType() == TransactionType.COMMISSION_FEE
                                && t.getOrderId().startsWith("FEE_KAFKA_") && bookingIds.contains(t.getBookingId()))
                        .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }

    @PostMapping("/admin/food-orders/{id}/sync-payment")
    public ResponseEntity<Void> syncFoodPayment(@PathVariable Long id) {
        var paid = transactionRepository.findByBookingIdAndTransactionTypeOrderByIdDesc(id, TransactionType.FOOD_PAYMENT).stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS && transactionRepository.findByOrderId("REFUND_PAYMENT_" + t.getId()).isEmpty()).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Chưa có giao dịch thành công được xác thực"));
        workflow.fulfill(paid.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/wallets")
    public ResponseEntity<List<Wallet>> getAllWalletsAdmin() {
        return ResponseEntity.ok(walletRepository.findAll());
    }

    @GetMapping("/wallet")
    public ResponseEntity<WalletResponse> getMyWallet(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String roleStr) {
        UserType userType = parseUserType(roleStr);
        return ResponseEntity.ok(paymentMapper.toWalletResponse(walletService.getOrCreateWallet(userId, userType)));
    }

    private UserType parseUserType(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return UserType.CUSTOMER;
        }
        String clean = roleStr.toUpperCase().replace("ROLE_", "").trim();
        if ("MERCHANT".equals(clean)) {
            return UserType.RESTAURANT;
        }
        try {
            return UserType.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return UserType.CUSTOMER;
        }
    }

    @GetMapping("/booking/{bookingId}/payment-status")
    public ResponseEntity<String> checkPaymentStatus(@PathVariable Long bookingId) {
        boolean isSuccess = transactionRepository.findByBookingId(bookingId)
                .stream()
                .anyMatch(tx -> tx.getTransactionType() == TransactionType.TRIP_PAYMENT
                        && tx.getStatus() == TransactionStatus.SUCCESS
                        && transactionRepository.findByOrderId("REFUND_PAYMENT_" + tx.getId()).isEmpty());
        return ResponseEntity.ok(isSuccess ? "SUCCESS" : "PENDING");
    }

    @GetMapping("/food-order/{orderId}/payment-status")
    public ResponseEntity<String> checkFoodOrderPaymentStatus(@PathVariable Long orderId) {
        boolean isSuccess = transactionRepository.findByBookingId(orderId)
                .stream()
                .anyMatch(tx -> (tx.getTransactionType() == TransactionType.FOOD_PAYMENT)
                        && tx.getStatus() == TransactionStatus.SUCCESS
                        && transactionRepository.findByOrderId("REFUND_PAYMENT_" + tx.getId()).isEmpty());
        return ResponseEntity.ok(isSuccess ? "SUCCESS" : "PENDING");
    }

    @PostMapping("/internal/food-order-payout")
    public ResponseEntity<Void> payoutFoodOrder(@RequestBody FoodOrderPayoutRequest request) {
        log.info("Nhận yêu cầu payout đơn giao đồ ăn: {}", request);
        workflow.reconcile(true, request.getOrderId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/refund-order")
    public ResponseEntity<Void> refundOrder(@RequestBody RefundRequest request) {
        log.info("Nhận yêu cầu hoàn tiền cho đơn hàng: {}", request);
        workflow.reconcile(true, request.getOrderId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payment/create")
    public ResponseEntity<PaymentUrlResponse> createUniversalPayment(
            @RequestHeader("Idempotency-Key") String key,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String method,
            @RequestParam String type,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long driverId,
            @RequestParam BigDecimal amount) {

        log.info("Khởi tạo thanh toán. Cổng: {}, Luồng: {}, Số tiền: {}", method, type, amount);

        PaymentUrlResponse response = paymentIdempotency.create(key, userId, role, method, type, bookingId, amount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String roleStr) {
        UserType userType = parseUserType(roleStr);
        var wallet = walletService.getOrCreateWallet(userId, userType);
        return ResponseEntity.ok(transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()));
    }

    @PostMapping("/wallet/deposit")
    public ResponseEntity<WalletResponse> depositWallet(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "CUSTOMER") String roleStr,
            @RequestParam Double amount,
            @RequestParam(required = false, defaultValue = "Nạp tiền vào ví") String note) {
        throw new ResponseStatusException(HttpStatus.GONE,
                "Nạp tiền phải qua cổng thanh toán được xác thực");
    }

    @PostMapping("/wallet/withdraw")
    public ResponseEntity<Void> withdrawWallet(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "CUSTOMER") String roleStr,
            @RequestParam Double amount) {
        UserType userType = parseUserType(roleStr);
        walletService.withdrawWallet(userId, userType, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/driver/withdraw")
    public ResponseEntity<Void> withdrawDriverWallet(
            @RequestHeader("X-User-Id") Long driverId,
            @RequestParam Double amount) {
        walletService.withdrawWallet(driverId, UserType.DRIVER, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> handleMomoIPN(@RequestBody MomoIpnRequest request) {
        momoService.processMomoIpn(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/momo/return")
    public ResponseEntity<String> handleMomoReturn(@RequestParam("orderId") String orderId) {
        return renderStoredResult(orderId, PaymentMethod.MOMO);
    }
    @GetMapping("/vnpay/return")
    public ResponseEntity<String> handleVnpayReturn(@RequestParam Map<String, String> parameters) {
        String orderId = parameters.get("vnp_TxnRef");
        if (orderId == null) {
            orderId = "";
        }
        if (parameters.containsKey("vnp_SecureHash")) {
            try {
                vnpayService.processIpn(parameters);
            } catch (Exception e) {
                log.warn("Lỗi xử lý vnpay/return qua processIpn: {}", e.getMessage());
            }
        }
        return renderStoredResult(orderId, PaymentMethod.VNPAY);
    }
    @GetMapping("/vnpay/ipn")
    public Map<String,String> handleVnpayIpn(@RequestParam Map<String,String> parameters) {
        return vnpayService.processIpn(parameters);
    }
    private ResponseEntity<String> renderStoredResult(String orderId, PaymentMethod method) {
        Transaction tx = transactionRepository.findByOrderId(orderId).filter(t -> t.getPaymentMethod() == method).orElse(null);
        String code = tx != null && tx.getStatus() == TransactionStatus.SUCCESS ? "00" : "PENDING";
        return renderReturnHtml(code, HtmlUtils.htmlEscape(orderId),
                "Trạng thái lấy từ hệ thống. Nếu đang chờ xác nhận, vui lòng kiểm tra lại trong ứng dụng.");
    }

    @PutMapping("/booking/{bookingId}/cancel-pending")
    public ResponseEntity<Void> cancelPendingPayments(@PathVariable Long bookingId) {
        log.info("Yêu cầu hủy các giao dịch PENDING của cuốc xe #{}", bookingId);
        walletService.cancelPendingTransactionsByBookingId(bookingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactionsAdmin() {
        return ResponseEntity.ok(transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminPaymentStats() {
        List<Transaction> all = transactionRepository.findAll();
        long total = all.size();
        long successCount = all.stream().filter(t -> t.getStatus() == TransactionStatus.SUCCESS).count();
        BigDecimal totalVolume = all.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS && t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> map = new HashMap<>();
        map.put("totalTransactions", total);
        map.put("successTransactions", successCount);
        map.put("totalVolume", totalVolume);
        map.put("netRevenue", all.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS && t.getTransactionType() == TransactionType.COMMISSION_FEE)
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

        return ResponseEntity.ok(map);
    }

    private ResponseEntity<String> renderReturnHtml(String code, String orderId, String subText) {
        String title = "00".equals(code) ? "Thanh Toán Thành Công!" : "Chưa xác nhận thanh toán";
        String icon = "00".equals(code) ? "✅" : "❌";
        String color = "00".equals(code) ? "#2e7d32" : "#d32f2f";

        String html = """
                <!DOCTYPE html><html><head><meta charset='UTF-8'><title>Kết Quả</title>
                <style>body{font-family:'Segoe UI';background:#f4f6f9;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}
                .card{background:#fff;padding:40px;border-radius:16px;box-shadow:0 10px 25px rgba(0,0,0,0.08);text-align:center;max-width:400px;width:90%%;}
                h2{color:%s;}.btn{display:inline-block;background:#ae2070;color:#white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:bold;}</style></head>
                <body><div class='card'><h2>%s %s</h2><p>Mã đơn: %s</p><p>%s</p><a href='javascript:window.close()' class='btn' style='color:white;'>Đóng Cửa Sổ</a></div></body></html>
                """.formatted(color, icon, title, orderId, subText);
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }
}