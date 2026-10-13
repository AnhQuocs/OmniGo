package com.trung.bookingservice.service;

import com.trung.bookingservice.dto.response.BookingDriverResponse;
import com.trung.bookingservice.entity.Booking;
import com.trung.bookingservice.repository.BookingRepository;
import com.trung.bookingservice.service.client.UserDriverClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingDriverService {
    private final BookingRepository bookingRepository;
    private final UserDriverClient userDriverClient;

    public BookingDriverResponse getDriver(Long bookingId, Long customerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyến xe"));
        if (customerId == null || !customerId.equals(booking.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không sở hữu chuyến xe này");
        }
        if (booking.getDriverId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chuyến xe chưa có tài xế được phân công");
        }

        try {
            var response = userDriverClient.getDriverProfile(booking.getDriverId());
            var body = response != null ? response.getBody() : null;
            var driver = body != null ? body.getData() : null;
            if (response == null || !response.getStatusCode().is2xxSuccessful()
                    || body == null || !body.isSuccess() || driver == null
                    || !Objects.equals(booking.getDriverId(), driver.driverId())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không lấy được thông tin tài xế");
            }
            return new BookingDriverResponse(driver.driverId(), driver.fullName(), driver.phoneNumber(),
                    driver.licensePlate(), driver.vehicleType(), driver.vehicleModel(), driver.avatarUrl());
        } catch (FeignException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không lấy được thông tin tài xế", ex);
        }
    }
}
