package com.trung.paymentservice.scheduler;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.util.enums.TransactionStatus;
import com.trung.paymentservice.service.PaymentWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Component @RequiredArgsConstructor @Slf4j
public class PaymentScheduler {
    private final TransactionRepository transactionRepository;
    private final PaymentWorkflowService workflow;
    @Scheduled(cron = "0 */5 * * * *")
    public void cleanExpiredPendingTransactions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        for (var tx : transactionRepository.findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, cutoff)) {
            try { workflow.expire(tx.getOrderId(), cutoff); }
            catch (Exception e) { log.warn("Could not expire payment {}: {}", tx.getId(), e.getClass().getSimpleName()); }
        }
    }
}
