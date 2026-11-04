package com.trung.paymentservice.scheduler;
import com.trung.paymentservice.repository.TransactionRepository;
import com.trung.paymentservice.service.PaymentWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.domain.PageRequest;
@Component @RequiredArgsConstructor @Slf4j
public class PaymentFulfillmentScheduler {
    private final TransactionRepository transactions;
    private final PaymentWorkflowService workflow;
    @Scheduled(fixedDelay = 5000)
    public void retry() {
        for (Long id : transactions.findUnfulfilled(PageRequest.of(0, 100))) {
            try { workflow.fulfill(id); }
            catch (Exception e) { transactions.postponeFulfillment(id); log.warn("Payment fulfillment pending for transaction {}: {}", id, e.getClass().getSimpleName()); }
        }
    }
}
