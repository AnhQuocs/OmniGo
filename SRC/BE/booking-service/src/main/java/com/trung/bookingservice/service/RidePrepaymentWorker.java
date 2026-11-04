package com.trung.bookingservice.service;
import com.trung.bookingservice.service.client.PaymentClient;
import com.trung.bookingservice.service.impl.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
@Service @RequiredArgsConstructor @Slf4j
public class RidePrepaymentWorker {
    private final JdbcTemplate jdbc;
    private final PaymentClient payments;
    private final BookingServiceImpl bookings;
    public void pay(Long id) {
        var booking = bookings.currentResponse(id);
        if (booking.getStatus() != com.trung.bookingservice.util.enums.BookingStatus.AWAITING_PAYMENT) return;
        try { payments.prepay(id); }
        catch (feign.FeignException error) {
            if (error.status() == 400 || error.status() == 422) {
                try { bookings.cancelBooking(booking.getCustomerId(), id); }
                catch (Exception cancelError) { log.warn("Prepayment cancellation pending for {}", id, cancelError); }
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Không thể thanh toán OmniPay. Hãy kiểm tra số dư ví.");
            }
            log.warn("Ride prepayment pending for {}: {}", id, error.status());
        }
    }
    @Scheduled(fixedDelay=10000)
    public void retry() {
        for (Long id : jdbc.queryForList("select id from bookings where status='AWAITING_PAYMENT' and prepaid=true order by id limit 50", Long.class)) {
            try { pay(id); } catch (Exception error) { log.warn("Ride prepayment retry {}: {}", id, error.getClass().getSimpleName()); }
        }
    }
}
