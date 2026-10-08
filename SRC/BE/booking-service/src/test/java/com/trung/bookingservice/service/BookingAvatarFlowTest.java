package com.trung.bookingservice.service;

import com.trung.bookingservice.entity.Booking;
import com.trung.bookingservice.repository.BookingRepository;
import com.trung.bookingservice.service.client.UserDriverClient;
import com.trung.bookingservice.service.impl.BookingServiceImpl;
import com.trung.bookingservice.util.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingAvatarFlowTest {
    private final BookingRepository repository = mock(BookingRepository.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class, RETURNS_DEEP_STUBS);
    private final SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
    private final BookingAvatarService avatars = mock(BookingAvatarService.class);
    private final BookingServiceImpl service = new BookingServiceImpl(repository, null, messaging, redis,
            mock(UserDriverClient.class), null, null, null, null, null, avatars);

    private Booking booking(BookingStatus status) {
        Booking booking = Booking.builder().id(1L).customerId(2L).status(status)
                .customerAvatarUrl("https://example.com/customer.png")
                .startLatitude(10.0).startLongitude(106.0).endLatitude(10.1).endLongitude(106.1).build();
        when(repository.findById(1L)).thenReturn(Optional.of(booking));
        return booking;
    }

    @Test
    void acceptingRidePersistsBothAvatarsAndPublishesThem() throws Exception {
        Booking booking = booking(BookingStatus.PENDING);
        when(redis.opsForValue().get("drivers:reserved:3")).thenReturn("1");
        when(avatars.getAvatar(3L)).thenReturn("https://example.com/driver.png");
        var response = service.acceptBooking(3L, 1L);
        assertEquals("https://example.com/driver.png", booking.getDriverAvatarUrl());
        assertEquals(booking.getCustomerAvatarUrl(), response.getCustomerAvatarUrl());
        assertEquals(booking.getDriverAvatarUrl(), response.getDriverAvatarUrl());
        verify(repository).save(booking);
        verify(messaging).convertAndSendToUser("2", "/queue/booking/status", response);
    }

    @Test
    void cancellingAssignedDriverClearsOldAvatarButKeepsCustomerAvatar() throws Exception {
        Booking booking = booking(BookingStatus.ACCEPTED);
        booking.setDriverId(3L);
        booking.setDriverAvatarUrl("https://example.com/old-driver.png");
        TransactionSynchronizationManager.initSynchronization();
        try {
            var response = service.cancelBookingByDriver(3L, 1L);
            assertNull(booking.getDriverId());
            assertNull(booking.getDriverAvatarUrl());
            assertNull(response.getDriverAvatarUrl());
            assertEquals("https://example.com/customer.png", response.getCustomerAvatarUrl());
            verify(repository).save(booking);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
