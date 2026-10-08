package com.trung.bookingservice.service;

import com.trung.bookingservice.dto.response.ApiResponse;
import com.trung.bookingservice.dto.response.DriverProfileResponse;
import com.trung.bookingservice.entity.Booking;
import com.trung.bookingservice.repository.BookingRepository;
import com.trung.bookingservice.service.client.UserDriverClient;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingDriverServiceTest {
    private final BookingRepository repository = mock(BookingRepository.class);
    private final UserDriverClient client = mock(UserDriverClient.class);
    private final BookingDriverService service = new BookingDriverService(repository, client);

    @BeforeEach
    void setUp() {
        when(repository.findById(1L)).thenReturn(Optional.of(
                Booking.builder().id(1L).customerId(2L).driverId(3L).build()));
    }

    @Test
    void returnsCustomerFieldsForAssignedDriver() {
        when(client.getDriverProfile(3L)).thenReturn(ResponseEntity.ok(ApiResponse.<DriverProfileResponse>builder()
                .success(true).data(new DriverProfileResponse(3L, "Driver", "0901234567", "59-A1", "BIKE", "Wave", "https://example.com/avatar.jpg")).build()));
        var result = service.getDriver(1L, 2L);
        assertEquals(3L, result.driverId());
        assertEquals("Driver", result.driverName());
        assertEquals("0901234567", result.driverPhone());
        assertEquals("59-A1", result.vehiclePlate());
        assertEquals("BIKE", result.vehicleType());
        assertEquals("Wave", result.vehicleModel());
        assertEquals("https://example.com/avatar.jpg", result.avatarUrl());
    }

    @Test
    void rejectsOtherCustomersBeforeCallingDriverService() {
        assertStatus(HttpStatus.FORBIDDEN, () -> service.getDriver(1L, 99L));
        verifyNoInteractions(client);
    }

    @Test
    void missingBookingReturns404() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getDriver(1L, 2L));
        verifyNoInteractions(client);
    }

    @Test
    void unassignedBookingReturns404() {
        when(repository.findById(1L)).thenReturn(Optional.of(Booking.builder().customerId(2L).build()));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getDriver(1L, 2L));
        verifyNoInteractions(client);
    }

    @Test
    void upstreamFailureReturns502() {
        when(client.getDriverProfile(3L)).thenThrow(mock(FeignException.class));
        assertStatus(HttpStatus.BAD_GATEWAY, () -> service.getDriver(1L, 2L));
    }

    @Test
    void emptyUpstreamResponseReturns502() {
        when(client.getDriverProfile(3L)).thenReturn(ResponseEntity.ok().build());
        assertStatus(HttpStatus.BAD_GATEWAY, () -> service.getDriver(1L, 2L));
    }

    @Test
    void mismatchedDriverIsNeverReturned() {
        when(client.getDriverProfile(3L)).thenReturn(ResponseEntity.ok(ApiResponse.<DriverProfileResponse>builder()
                .success(true).data(new DriverProfileResponse(99L, "Other", null, null, null, null, null)).build()));
        assertStatus(HttpStatus.BAD_GATEWAY, () -> service.getDriver(1L, 2L));
    }

    private void assertStatus(HttpStatus status, org.junit.jupiter.api.function.Executable action) {
        assertEquals(status, assertThrows(ResponseStatusException.class, action).getStatusCode());
    }
}
