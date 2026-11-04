package com.trung.pricingservice.service;

import com.trung.pricingservice.controller.PricingController;
import com.trung.pricingservice.dto.request.PricingRequest;
import com.trung.pricingservice.dto.response.PricingResponse;
import com.trung.pricingservice.dto.route.RouteResult;
import com.trung.pricingservice.entity.PricingConfig;
import com.trung.pricingservice.mapper.PricingMapper;
import com.trung.pricingservice.repository.PricingConfigRepository;
import com.trung.pricingservice.service.client.LocationClient;
import com.trung.pricingservice.service.impl.PricingServiceImpl;
import com.uber.h3core.H3Core;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PricingSyncTest {
    private final PricingConfigRepository repository = mock(PricingConfigRepository.class);
    private final RouteCalculationService route = mock(RouteCalculationService.class);
    private final LocationClient location = mock(LocationClient.class);
    private final H3Core h3 = mock(H3Core.class);
    private final PricingServiceImpl service = new PricingServiceImpl(repository, new PricingMapper(), h3, route, location);

    private PricingRequest request() {
        return PricingRequest.builder().customerId(38L).startLatitude(21.0).startLongitude(105.0)
                .endLatitude(21.1).endLongitude(105.1).vehicleType("BIKE").build();
    }

    @Test
    void authenticatedPreviewUsesBookingCustomerIdentity() {
        PricingRequest request = request();
        request.setCustomerId(null);
        PricingService pricing = mock(PricingService.class);
        new PricingController(pricing, route).calculatePrice(request, 10, 38L);
        assertEquals(38L, request.getCustomerId());
        verify(pricing).calculateDynamicPrice(request, 10);
    }

    @Test
    void cachedQuoteKeepsDisplayedPriceWithoutRecountingDemand() {
        PricingRequest request = request();
        PricingResponse quote = PricingResponse.builder().totalPrice(32000).build();
        when(repository.getRideQuote(request)).thenReturn(quote);
        assertSame(quote, service.calculateDynamicPrice(request, 0));
        verifyNoInteractions(location, route, h3);
        verify(repository, never()).recordDemand(anyString(), anyString());
    }

    @Test
    void freshPriceUsesOnlineDriversWithinFiveKmInsteadOfClientCount() {
        PricingRequest request = request();
        when(location.getNearbyDrivers(105.0, 21.0, 5.0)).thenReturn(List.of(new LocationClient.NearbyDriver(1L)));
        when(h3.latLngToCellAddress(21.0, 105.0, 8)).thenReturn("cell");
        when(repository.getDemand("cell")).thenReturn(1);
        when(repository.getBasePricingConfig()).thenReturn(PricingConfig.builder().build());
        when(route.getRouteInfo(21.0, 105.0, 21.1, 105.1, "BIKE"))
                .thenReturn(RouteResult.builder().distanceKm(3).durationMinutes(10).build());
        when(repository.saveRideQuote(eq(request), any())).thenAnswer(call -> call.getArgument(1));
        PricingResponse response = service.calculateDynamicPrice(request, 0);
        assertEquals(1.0, response.getSurgeMultiplier());
        verify(repository).recordDemand("cell", "38");
        verify(location).getNearbyDrivers(105.0, 21.0, 5.0);
    }

    @Test
    void redisQuoteRoundTripPreservesTypeAndPrice() {
        RedisSerializer<Object> serializer = RedisSerializer.json();
        PricingResponse response = PricingResponse.builder().totalPrice(32000).distanceInKm(3.2).build();
        Object restored = serializer.deserialize(serializer.serialize(response));
        assertInstanceOf(PricingResponse.class, restored);
        assertEquals(response, restored);
    }
}
