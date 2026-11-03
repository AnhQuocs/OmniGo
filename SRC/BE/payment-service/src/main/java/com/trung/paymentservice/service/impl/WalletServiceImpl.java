package com.trung.paymentservice.service.impl;

import com.trung.paymentservice.dto.request.FoodOrderPayoutRequest;
import com.trung.paymentservice.dto.request.RefundRequest;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.entity.Wallet;
import com.trung.paymentservice.event.BookingCompletedEvent;
import com.trung.paymentservice.event.DriverRegisteredEvent;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.repository.WalletRepository;
import com.trung.paymentservice.service.WalletService;
import com.trung.paymentservice.util.enums.PaymentMethod;
import com.trung.paymentservice.util.enums.TransactionStatus;
import com.trung.paymentservice.util.enums.TransactionType;
import com.trung.paymentservice.util.enums.UserType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import com.trung.paymentservice.service.PlatformFeeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final PlatformFeeService platformFeeService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate internalRestTemplate;

    @Transactional
    @KafkaListener(topics = "driver-registered-topic", groupId = "payment-service")
    public void handleDriverRegistered(DriverRegisteredEvent event) {
        if (event == null || event.getDriverId() == null) {
            log.error("Dữ liệu Kafka Event driver-registered không hợp lệ.");
            return;
        }

        Long driverId = event.getDriverId();

        boolean exists = walletRepository.findByUserIdAndUserType(driverId, UserType.DRIVER).isPresent();
        if (exists) {
            log.info("Ví của Driver ID {} đã tồn tại từ trước, không cần tạo mới.", driverId);
            return;
        }

        Wallet wallet = Wallet.builder()
                .userId(driverId)
                .userType(UserType.DRIVER)
                .balance(BigDecimal.ZERO)
                .build();

        walletRepository.save(wallet);
        log.info("Kafka: Khởi tạo thành công Ví 0 VND cho Driver mới đăng ký, ID: {}", driverId);
    }

    @Override
    @Transactional
    public Wallet getOrCreateWallet(Long userId, UserType userType) {
        try {
            return walletRepository.findByUserIdAndUserType(userId, userType)
                    .orElseGet(() -> walletRepository.save(Wallet.builder()
                            .userId(userId)
                            .userType(userType)
                            .balance(BigDecimal.ZERO)
                            .build()));
        } catch (Exception e) {
            log.error("Lỗi xung đột khi tự động khởi tạo ví cho User {}, thử truy vấn lại: {}", userId, e.getMessage());
            return walletRepository.findByUserIdAndUserType(userId, userType)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Không thể khởi tạo ví cho User ID " + userId + " sau nhiều lần thử. Vui lòng thử lại sau."
                    ));
        }
    }

    @Override
    @Transactional
    public Wallet creditWallet(Long userId, UserType userType, Double amount) {
        Wallet wallet = walletRepository.findByUserIdAndUserTypeWithLock(userId, userType)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .userId(userId)
                        .userType(userType)
                        .balance(BigDecimal.ZERO)
                        .build()));

        BigDecimal bdAmount = BigDecimal.valueOf(amount);
        wallet.setBalance(wallet.getBalance().add(bdAmount));
        log.info("Đã cộng {} VND vào ví của User ID {}. Số dư mới: {} VND",
                amount, userId, wallet.getBalance());
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    @KafkaListener(topics = "booking-completed-topic", groupId = "payment-service")
    public void deductCommission(BookingCompletedEvent event) {
        Long driverId = event.getDriverId();
        Long bookingId = event.getBookingId();
        Double tripAmount = event.getAmount();

        if (driverId == null || bookingId == null || tripAmount == null) {
            log.error("Dữ liệu Kafka Event không hợp lệ: {}", event);
            return;
        }

        getOrCreateWallet(driverId, UserType.DRIVER);
        Wallet wallet = walletRepository.findByUserIdAndUserTypeWithLock(driverId, UserType.DRIVER)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy ví của tài xế ID " + driverId + " để trừ phí hoa hồng."
                ));

        boolean alreadyDeducted = transactionRepository.findByWalletIdOrderByCreatedAtDesc(
                        wallet.getId())
                .stream().anyMatch(tx -> tx.getBookingId() != null
                        && tx.getBookingId().equals(bookingId)
                        && tx.getTransactionType() == TransactionType.COMMISSION_FEE
                        && tx.getOrderId().startsWith("FEE_KAFKA_"));

        if (alreadyDeducted) {
            log.info("Cuốc xe #{} đã được thu hoa hồng trước đó, bỏ qua trừ lần 2.", bookingId);
            return;
        }

        BigDecimal commissionFee = platformFeeService.calculate(BigDecimal.valueOf(tripAmount), platformFeeService.getPercentage());


        if (wallet.getBalance().compareTo(commissionFee) < 0) {
            log.warn("Ví tài xế không đủ tiền trừ phí sàn. Ghi nhận công nợ âm.");
        }

        wallet.setBalance(wallet.getBalance().subtract(commissionFee));
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .bookingId(bookingId)
                .orderId("FEE_KAFKA_" + bookingId + "_" + System.currentTimeMillis())
                .amount(commissionFee)
                .transactionType(TransactionType.COMMISSION_FEE)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(transaction);

        log.info("Đã thu hoa hồng sau cuốc xe #{}: -{} VND", bookingId, commissionFee);
        if (wallet.getBalance().compareTo(BigDecimal.valueOf(-50000)) <= 0) {
            log.warn("Tài khoản Driver {} nợ cước hệ thống quá hạn mức (-50k). Số dư: {}. Ép OFFLINE!", driverId, wallet.getBalance());
            try {
                String lockUrl = "http://USER-DRIVER-SERVICE/api/v1/drivers/" + driverId + "/status?isActive=false";
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", String.valueOf(driverId));

                HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
                internalRestTemplate.exchange(lockUrl, HttpMethod.PUT, requestEntity, Void.class);

                log.info("Đã kích hoạt cơ chế đá tài xế {} về OFFLINE thành công.", driverId);
            } catch (Exception e) {
                log.error("Lỗi khi kết nối gọi ép Offline: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public Wallet depositWallet(Long userId, UserType userType, Double amount, String note) {
        if (amount == null || amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền nạp phải lớn hơn 0!");
        }

        Wallet wallet = getOrCreateWallet(userId, userType);
        BigDecimal bdAmount = BigDecimal.valueOf(amount);
        wallet.setBalance(wallet.getBalance().add(bdAmount));
        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .walletId(savedWallet.getId())
                .orderId("DEP_" + userType + "_" + userId + "_" + System.currentTimeMillis())
                .amount(bdAmount)
                .transactionType(TransactionType.DEPOSIT)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(transaction);

        log.info("Nạp tiền thành công cho User ID {} ({}): +{} VND. Số dư mới: {} VND",
                userId, userType, amount, savedWallet.getBalance());
        return savedWallet;
    }

    @Override
    @Transactional
    public void withdrawWallet(Long driverId, Double amount) {
        withdrawWallet(driverId, UserType.DRIVER, amount);
    }

    @Override
    @Transactional
    public void withdrawWallet(Long userId, UserType userType, Double amount) {
        if (amount == null || amount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền rút phải lớn hơn 0!"
            );
        }
        BigDecimal bdAmount = BigDecimal.valueOf(amount);
        Wallet wallet = walletRepository.findByUserIdAndUserTypeWithLock(userId, userType)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Không tìm thấy ví của " + userType + " ID " + userId
                ));

        if (wallet.getBalance().compareTo(bdAmount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số dư ví không đủ để thực hiện lệnh rút tiền này! (Số dư hiện tại: " + wallet.getBalance() + " đ)"
            );
        }

        wallet.setBalance(wallet.getBalance().subtract(bdAmount));
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .orderId("WITHDRAW_" + userType + "_" + userId + "_" + System.currentTimeMillis())
                .amount(bdAmount)
                .transactionType(TransactionType.WITHDRAWAL)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(transaction);

        log.info("{} ID {} đã rút thành công {} VND. Số dư còn lại: {} VND",
                userType, userId, amount, wallet.getBalance());
    }

    @Override
    @Transactional
    public void cancelPendingTransactionsByBookingId(Long bookingId) {
        List<Transaction> pendingTxs = transactionRepository.findByBookingIdAndStatus(bookingId, TransactionStatus.PENDING);
        if (!pendingTxs.isEmpty()) {
            for (Transaction tx : pendingTxs) {
                tx.setStatus(TransactionStatus.CANCELED);
            }
            transactionRepository.saveAll(pendingTxs);
            log.info("Đã chuyển trạng thái sang CANCELED cho {} giao dịch của booking {}", pendingTxs.size(), bookingId);
        }
    }

    @Override
    @Transactional
    public void processFoodOrderPayout(FoodOrderPayoutRequest request) {
        if (request == null || request.getDriverId() == null || request.getOrderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu mã đơn hàng hoặc tài xế");
        }

        Long driverId = request.getDriverId();
        Long orderId = request.getOrderId();
        BigDecimal deliveryFee = request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.valueOf(15000);
        if (deliveryFee.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phí giao hàng không được âm");
        }
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH";

        Wallet wallet = walletRepository.findByUserIdAndUserTypeWithLock(driverId, UserType.DRIVER)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .userId(driverId)
                        .userType(UserType.DRIVER)
                        .balance(BigDecimal.ZERO)
                        .build()));

        BigDecimal percentage = platformFeeService.getPercentage();
        settleRestaurant(request, percentage);

        boolean alreadyProcessed = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream().anyMatch(tx -> tx.getOrderId() != null && (tx.getOrderId().startsWith("FOOD_FEE_" + orderId + "_") || tx.getOrderId().startsWith("FOOD_INC_" + orderId + "_")));

        if (alreadyProcessed) {
            log.info("Đơn đồ ăn #{} đã được quyết toán hoa hồng/cước phí trước đó, bỏ qua.", orderId);
            return;
        }

        BigDecimal commissionFee = platformFeeService.calculate(deliveryFee, percentage);
        BigDecimal netIncome = deliveryFee.subtract(commissionFee);

        // Cộng thu nhập ròng tiền ship (sau khi trừ phí sàn cấu hình) vào ví tài xế
        wallet.setBalance(wallet.getBalance().add(netIncome));
        walletRepository.save(wallet);

        // Ghi nhận giao dịch cộng cước ship (+deliveryFee)
        Transaction incTx = Transaction.builder()
                .walletId(wallet.getId())
                .bookingId(orderId)
                .orderId("FOOD_INC_" + orderId + "_" + System.currentTimeMillis())
                .amount(deliveryFee)
                .transactionType(TransactionType.FOOD_DELIVERY_INCOME)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(incTx);

        // Ghi nhận giao dịch khấu trừ phí sàn (-commissionFee)
        Transaction feeTx = Transaction.builder()
                .walletId(wallet.getId())
                .bookingId(orderId)
                .orderId("FOOD_FEE_" + orderId + "_" + System.currentTimeMillis())
                .amount(commissionFee)
                .transactionType(TransactionType.COMMISSION_FEE)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(feeTx);

        log.info("Đã cộng cước ship đơn đồ ăn #{} vào ví tài xế ID {}: +{} VND (Cước: {} VND, Phí sàn: -{} VND). Phương thức thanh toán: {}",
                orderId, driverId, netIncome, deliveryFee, commissionFee, paymentMethod);

        if (wallet.getBalance().compareTo(BigDecimal.valueOf(-50000)) <= 0) {
            log.warn("Tài xế ID {} nợ cước hệ thống quá hạn mức (-50k). Số dư: {}. Ép OFFLINE!", driverId, wallet.getBalance());
            try {
                String lockUrl = "http://USER-DRIVER-SERVICE/api/v1/drivers/" + driverId + "/status?isActive=false";
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", String.valueOf(driverId));
                HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
                internalRestTemplate.exchange(lockUrl, HttpMethod.PUT, requestEntity, Void.class);
            } catch (Exception e) {
                log.error("Lỗi gọi khóa tài xế ID {}: {}", driverId, e.getMessage());
            }
        }
    }

    private void settleRestaurant(FoodOrderPayoutRequest request, BigDecimal percentage) {
        if (request.getRestaurantOwnerId() == null || request.getFoodAmount() == null || request.getFoodAmount().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu thông tin quyết toán quán ăn");
        }
        Long ownerId = request.getRestaurantOwnerId();
        getOrCreateWallet(ownerId, UserType.RESTAURANT);
        Wallet wallet = walletRepository.findByUserIdAndUserTypeWithLock(ownerId, UserType.RESTAURANT).orElseThrow();
        String feeId = "FOOD_RESTAURANT_FEE_" + request.getOrderId();
        if (transactionRepository.findByOrderId(feeId).isPresent()) return;
        BigDecimal fee = platformFeeService.calculate(request.getFoodAmount(), percentage);
        // For CASH, the restaurant already received the food amount from the driver.
        boolean cash = request.getPaymentMethod() == null || "CASH".equalsIgnoreCase(request.getPaymentMethod());
        wallet.setBalance(wallet.getBalance().add(cash ? fee.negate() : request.getFoodAmount().subtract(fee)));
        walletRepository.save(wallet);
        if (!cash) {
            transactionRepository.save(Transaction.builder().walletId(wallet.getId()).bookingId(request.getOrderId())
                    .orderId("FOOD_RESTAURANT_INC_" + request.getOrderId()).amount(request.getFoodAmount())
                    .transactionType(TransactionType.FOOD_RESTAURANT_INCOME).paymentMethod(PaymentMethod.WALLET)
                    .status(TransactionStatus.SUCCESS).build());
        }
        transactionRepository.save(Transaction.builder().walletId(wallet.getId()).bookingId(request.getOrderId())
                .orderId(feeId).amount(fee).transactionType(TransactionType.COMMISSION_FEE)
                .paymentMethod(PaymentMethod.WALLET).status(TransactionStatus.SUCCESS).build());
    }

    @Override
    @Transactional
    public void processRefund(RefundRequest request) {
        if (request == null || request.getUserId() == null || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Dữ liệu hoàn tiền không hợp lệ: {}", request);
            return;
        }

        Long userId = request.getUserId();
        UserType userType = "DRIVER".equalsIgnoreCase(request.getUserType()) ? UserType.DRIVER : UserType.CUSTOMER;
        BigDecimal amount = request.getAmount();
        Long orderId = request.getOrderId();

        Wallet wallet = getOrCreateWallet(userId, userType);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction refundTx = Transaction.builder()
                .walletId(wallet.getId())
                .bookingId(orderId)
                .orderId("REFUND_" + (orderId != null ? orderId : System.currentTimeMillis()) + "_" + System.currentTimeMillis())
                .amount(amount)
                .transactionType(TransactionType.REFUND)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(refundTx);

        log.info("Đã hoàn tiền {} VND cho User ID {} (Loại: {}) cho đơn #{} thành công. Số dư mới: {} VND",
                amount, userId, userType, orderId, wallet.getBalance());
    }
}