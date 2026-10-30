package com.trung.bookingservice.service;

import com.trung.bookingservice.service.client.UserDriverClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCustomerService {
    private final UserDriverClient userDriverClient;

    // Customer name is optional: an unavailable profile service must not prevent a ride.
    public String getName(Long userId) {
        if (userId == null) return null;
        try {
            var response = userDriverClient.getUserName(userId);
            var body = response != null ? response.getBody() : null;
            return response != null && response.getStatusCode().is2xxSuccessful()
                    && body != null && body.isSuccess() ? body.getData() : null;
        } catch (FeignException ex) {
            log.warn("Không lấy được tên cho user {}: HTTP {}", userId, ex.status());
            return null;
        }
    }
}
