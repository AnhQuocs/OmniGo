package com.trung.bookingservice.service;
import com.trung.bookingservice.dto.request.BookingRequest;
import com.trung.bookingservice.dto.response.PricingResponse;
import com.trung.bookingservice.entity.Booking;
import com.trung.bookingservice.repository.BookingRepository;
import com.trung.bookingservice.service.client.*;
import com.trung.bookingservice.service.impl.BookingServiceImpl;
import com.trung.bookingservice.util.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RidePrepaymentTest {
    final BookingRepository repository=mock(BookingRepository.class);
    final LocationClient location=mock(LocationClient.class);
    final PricingClient pricing=mock(PricingClient.class);
    final DispatchConfigService dispatch=mock(DispatchConfigService.class);
    final FinancialOutbox outbox=mock(FinancialOutbox.class);
    final SimpMessagingTemplate messaging=mock(SimpMessagingTemplate.class);
    final BookingServiceImpl service=new BookingServiceImpl(outbox,null,repository,location,messaging,
        mock(StringRedisTemplate.class,RETURNS_DEEP_STUBS),mock(UserDriverClient.class),null,pricing,null,null,dispatch,
        mock(BookingAvatarService.class),mock(BookingCustomerService.class),mock(DriverVehicleService.class));
    @Test void walletCreationWaitsWithoutInvitingDriver() throws Exception {
        when(dispatch.getMaxRideDistanceKm()).thenReturn(5.0);
        when(location.getNearbyDrivers(anyDouble(),anyDouble(),anyDouble())).thenReturn(List.of());
        PricingResponse quote=new PricingResponse(); quote.setTotalPrice(50000); quote.setDistanceInKm(2.0);
        when(pricing.calculatePrice(any(),anyInt())).thenReturn(quote);
        when(repository.save(any())).thenAnswer(i -> { Booking b=i.getArgument(0); b.setId(99L); return b; });
        BookingRequest request=new BookingRequest(); request.setPaymentMethod("WALLET");
        request.setStartLatitude(21.0);request.setStartLongitude(105.0);request.setEndLatitude(21.1);request.setEndLongitude(105.1);
        var result=service.createBooking(1L,request);
        assertEquals(BookingStatus.AWAITING_PAYMENT,result.getStatus());
        assertTrue(result.isPrepaid()); assertFalse(result.isPaid()); assertEquals("WALLET",result.getPaymentMethod());
        verifyNoInteractions(messaging,outbox);
    }
    @Test void voucherIsRedeemedBeforePrepaymentWithDiscountedPrice() throws Exception {
        when(dispatch.getMaxRideDistanceKm()).thenReturn(5.0);
        when(location.getNearbyDrivers(anyDouble(),anyDouble(),anyDouble())).thenReturn(List.of());
        PricingResponse quote=new PricingResponse(); quote.setTotalPrice(50000); quote.setDistanceInKm(2.0);
        when(pricing.calculatePrice(any(),anyInt())).thenReturn(quote);
        when(repository.save(any())).thenAnswer(i -> { Booking b=i.getArgument(0); b.setId(99L); return b; });
        BookingRequest request=new BookingRequest(); request.setPaymentMethod("WALLET");
        request.setStartLatitude(21.0);request.setStartLongitude(105.0);request.setEndLatitude(21.1);request.setEndLongitude(105.1);
        var vouchers=mock(OrderVoucherService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"orderVouchers",vouchers);
        request.setVoucherCode("SAVE20"); request.setExpectedPrice(50000.0);
        when(vouchers.redeem("SAVE20",1L,99L,new java.math.BigDecimal("50000.0"))).thenReturn(new java.math.BigDecimal("20000"));
        var result=service.createBooking(1L,request);
        assertEquals(30000.0,result.getPrice()); assertEquals("SAVE20",result.getVoucherCode());
        assertEquals(0,result.getOriginalAmount().compareTo(new java.math.BigDecimal("50000")));
        assertEquals(0,result.getDiscountAmount().compareTo(new java.math.BigDecimal("20000")));
        assertEquals(BookingStatus.AWAITING_PAYMENT,result.getStatus());
        assertTrue(result.isPrepaid()); assertFalse(result.isPaid()); assertEquals("WALLET",result.getPaymentMethod());
        verifyNoInteractions(messaging,outbox);
    }
    @Test void confirmedPrepaymentWithNoDriverCancelsAndSchedulesRefund() throws Exception {
        Booking b=active("WALLET",true,false); b.setStatus(BookingStatus.AWAITING_PAYMENT); b.setDriverId(null);
        when(dispatch.getMaxRideDistanceKm()).thenReturn(5.0);
        when(location.getNearbyDrivers(anyDouble(),anyDouble(),anyDouble())).thenReturn(List.of());
        var result=service.markPrepaid(99L);
        assertTrue(result.isPaid()); assertEquals(BookingStatus.CANCELLED,result.getStatus());
        verify(outbox).enqueue(99L);
        service.markPrepaid(99L); verify(outbox,times(1)).enqueue(99L);
    }
    private Booking active(String method,boolean prepaid,boolean paid) {
        Booking b=Booking.builder().id(99L).customerId(1L).driverId(11L).status(BookingStatus.IN_PROGRESS)
            .paymentMethod(method).prepaid(prepaid).paid(paid).price(50000.0).distanceInKm(2.0)
            .startLatitude(21.0).startLongitude(105.0).endLatitude(21.1).endLongitude(105.1).build();
        when(repository.findByIdWithLock(99L)).thenReturn(Optional.of(b));return b;
    }
    @Test void driverCannotOverrideCashWithWallet() throws Exception {
        active("CASH",false,false);
        assertEquals("CASH",service.completeTrip(11L,99L,"WALLET").getPaymentMethod());
        verify(outbox).enqueue(99L);
    }
    @Test void driverCannotOverridePrepaidWithCashAndRetryDoesNotEnqueueTwice() throws Exception {
        active("WALLET",true,true);
        var result=service.completeTrip(11L,99L,"CASH");
        assertEquals("WALLET",result.getPaymentMethod()); assertTrue(result.isPaid());
        service.completeTrip(11L,99L,"CASH"); verify(outbox,times(1)).enqueue(99L);
    }
    @Test void unpaidWalletRideCannotComplete() {
        active("WALLET",true,false);
        assertThrows(com.trung.bookingservice.exception.BadRequestException.class,()->service.completeTrip(11L,99L,"CASH"));
        verifyNoInteractions(outbox);
    }
}
