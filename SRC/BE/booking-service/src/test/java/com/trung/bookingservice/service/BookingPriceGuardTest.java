package com.trung.bookingservice.service;

import com.trung.bookingservice.dto.request.BookingRequest;
import com.trung.bookingservice.dto.response.PricingResponse;
import com.trung.bookingservice.exception.BadRequestException;
import com.trung.bookingservice.repository.BookingRepository;
import com.trung.bookingservice.service.client.LocationClient;
import com.trung.bookingservice.service.client.PricingClient;
import com.trung.bookingservice.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingPriceGuardTest {
    private final BookingRepository repository = mock(BookingRepository.class);
    private final LocationClient location = mock(LocationClient.class);
    private final PricingClient pricing = mock(PricingClient.class);
    private final DispatchConfigService dispatch = mock(DispatchConfigService.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class, RETURNS_DEEP_STUBS);
    private final BookingServiceImpl service = new BookingServiceImpl(repository, location, null, redis,
            null, null, pricing, null, null, dispatch, null, null, null);

    private BookingRequest request() {
        when(dispatch.getMaxRideDistanceKm()).thenReturn(5.0);
        when(location.getNearbyDrivers(anyDouble(), anyDouble(), anyDouble())).thenReturn(List.of());
        BookingRequest request = new BookingRequest();
        request.setStartLatitude(21.0);
        request.setStartLongitude(105.0);
        request.setEndLatitude(21.1);
        request.setEndLongitude(105.1);
        request.setExpectedPrice(32000.0);
        return request;
    }

    @Test
    void changedFareDoesNotCreateOrDispatchBooking() {
        BookingRequest request = request();
        PricingResponse response = new PricingResponse();
        response.setTotalPrice(48000);
        when(pricing.calculatePrice(any(), anyInt())).thenReturn(response);
        assertThrows(BadRequestException.class, () -> service.createBooking(38L, request));
        verify(repository, never()).save(any());
    }

    @Test
    void pricingFailureCannotSilentlyChargeFallbackFare() {
        BookingRequest request = request();
        when(pricing.calculatePrice(any(), anyInt())).thenThrow(new IllegalStateException("unavailable"));
        assertThrows(BadRequestException.class, () -> service.createBooking(38L, request));
        verify(repository, never()).save(any());
    }
}
