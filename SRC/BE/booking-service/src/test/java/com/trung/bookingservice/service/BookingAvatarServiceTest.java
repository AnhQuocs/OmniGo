package com.trung.bookingservice.service;

import com.trung.bookingservice.dto.response.ApiResponse;
import com.trung.bookingservice.service.client.UserDriverClient;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingAvatarServiceTest {
    private final UserDriverClient client = mock(UserDriverClient.class);
    private final BookingAvatarService service = new BookingAvatarService(client);

    @Test
    void returnsAvatarFromProfileService() {
        when(client.getUserAvatar(1L)).thenReturn(ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true).data("https://example.com/avatar.png").build()));
        assertEquals("https://example.com/avatar.png", service.getAvatar(1L));
    }

    @Test
    void unavailableProfileDoesNotBlockBooking() {
        when(client.getUserAvatar(1L)).thenThrow(mock(FeignException.class));
        assertNull(service.getAvatar(1L));
    }

    @Test
    void handlesMissingAvatarAndUnassignedDriver() {
        assertNull(service.getAvatar(null));
        verifyNoInteractions(client);
        when(client.getUserAvatar(1L)).thenReturn(ResponseEntity.ok(ApiResponse.<String>builder().success(true).build()));
        assertNull(service.getAvatar(1L));
    }
}
