package com.trung.paymentservice.service;
import com.trung.paymentservice.dto.response.PaymentUrlResponse;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.strategy.PaymentFactory;
import com.trung.paymentservice.util.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
@Service @RequiredArgsConstructor
public class PaymentRequestService {
    private final PaymentLocks locks;
    private final FinancialOrderClient orders;
    private final TransactionRepository transactions;
    private final PaymentFactory factory;
    @Transactional
    public PaymentUrlResponse create(Long userId, String role, String method, String type, Long id, BigDecimal amount) {
        method = method.toUpperCase(Locale.ROOT); type = type.toUpperCase(Locale.ROOT);
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0 || amount.compareTo(new BigDecimal("100000000")) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền VND không hợp lệ");
        if (!Set.of("WALLET", "MOMO", "VNPAY").contains(method)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cổng không hợp lệ");
        Long driverId = null;
        boolean food = "FOOD_PAYMENT".equals(type);
        if (food || "TRIP_PAYMENT".equals(type)) {
            if (!"CUSTOMER".equals(role.replace("ROLE_", ""))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            locks.lock((food ? "FOOD:" : "RIDE:") + id);
            Map<String,Object> order = orders.get(food, id);
            if (!Objects.equals(userId, FinancialOrderClient.id(order, "customerId"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Đơn không thuộc khách hàng");
            if (FinancialOrderClient.money(order, "amount").compareTo(amount) != 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền không khớp đơn hàng");
            TransactionType txType = food ? TransactionType.FOOD_PAYMENT : TransactionType.TRIP_PAYMENT;
            List<Transaction> existing = transactions.findByBookingIdAndTransactionTypeOrderByIdDesc(id, txType);
            for (Transaction tx : existing) {
                if (tx.getStatus() == TransactionStatus.SUCCESS && transactions.findByOrderId("REFUND_PAYMENT_" + tx.getId()).isEmpty()) return PaymentUrlResponse.builder().orderId(tx.getOrderId()).paymentUrl("ALREADY_PAID").build();
            }
            boolean prepay = !food && Boolean.TRUE.equals(order.get("prepaid")) && "WALLET".equals(method) && "AWAITING_PAYMENT".equals(order.get("status"));
            if (FinancialOrderClient.cancelled(order) || FinancialOrderClient.cash(order)
                    || (food && !"AWAITING_PAYMENT".equals(order.get("status")))
                    || (!food && !prepay && !"COMPLETED".equals(order.get("status"))))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Đơn không ở trạng thái cho phép thanh toán online");
            for (Transaction tx : existing) {
                if (tx.getStatus() == TransactionStatus.CANCELED)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Giao dịch cũ hết thời gian chờ; cần đối soát cổng thanh toán trước khi tạo giao dịch mới");
                if (tx.getStatus() == TransactionStatus.PENDING) {
                    if (!method.equals(tx.getPaymentMethod().name()) || tx.getPaymentUrl() == null)
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Giao dịch đang được xử lý; vui lòng kiểm tra trạng thái");
                    return PaymentUrlResponse.builder().orderId(tx.getOrderId()).paymentUrl(tx.getPaymentUrl()).build();
                }
            }
            driverId = FinancialOrderClient.id(order, "driverId");
            if (!food && !prepay && driverId == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cuốc xe chưa có tài xế");
        } else {
            String cleanRole = role.replace("ROLE_", "");
            boolean customer = "CUSTOMER_DEPOSIT".equals(type) && "CUSTOMER".equals(cleanRole);
            boolean driver = Set.of("DEPOSIT", "DRIVER_DEPOSIT").contains(type) && "DRIVER".equals(cleanRole);
            if ((!customer && !driver) || "WALLET".equals(method)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loại nạp ví không hợp lệ");
            id = null;
        }
        PaymentUrlResponse response = factory.getStrategy(method).createPayment(userId, driverId, id, amount, type);
        locks.lock(food ? "FOOD:" + id : "TRIP_PAYMENT".equals(type) ? "RIDE:" + id : "PAYMENT:" + response.getOrderId());
        Transaction tx = transactions.findByOrderId(response.getOrderId()).orElseThrow();
        tx.setDriverId(driverId); tx.setPaymentUrl(response.getPaymentUrl()); transactions.save(tx);
        return response;
    }
}
