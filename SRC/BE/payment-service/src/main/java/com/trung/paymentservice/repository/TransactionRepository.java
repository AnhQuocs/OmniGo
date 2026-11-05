package com.trung.paymentservice.repository;

import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.util.enums.TransactionStatus;
import com.trung.paymentservice.util.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByOrderId(String orderId);
    @Query(value = "select * from transactions where order_id = :orderId for update", nativeQuery = true)
    Optional<Transaction> findByOrderIdForUpdate(@Param("orderId") String orderId);
    List<Transaction> findByBookingIdAndTransactionTypeOrderByIdDesc(Long bookingId, TransactionType type);
    @Query("select t.id from Transaction t where t.status = TransactionStatus.SUCCESS and t.fulfillmentDone = false and (t.fulfillmentNextAttemptAt is null or t.fulfillmentNextAttemptAt <= CURRENT_TIMESTAMP) and t.transactionType in (TransactionType.FOOD_PAYMENT, TransactionType.TRIP_PAYMENT) order by t.id")
    List<Long> findUnfulfilled(Pageable pageable);
    @Transactional
    @Modifying
    @Query(value = "update transactions set fulfillment_attempts = fulfillment_attempts + 1, fulfillment_next_attempt_at = CURRENT_TIMESTAMP + interval '30 seconds' where id = :id and fulfillment_done = false", nativeQuery = true)
    void postponeFulfillment(@Param("id") Long id);

    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    List<Transaction> findByBookingId(Long bookingId);

    List<Transaction> findByBookingIdAndStatus(Long bookingId, TransactionStatus status);

    List<Transaction> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime cutoffTime);
}