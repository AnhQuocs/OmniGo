package com.trung.bookingservice.service;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.util.List;
import java.util.Map;
class OrderVoucherServiceTest {
    JdbcTemplate jdbc=mock(JdbcTemplate.class);
    OrderVoucherService service=new OrderVoucherService(jdbc,mock(PlatformTransactionManager.class));
    MockRestServiceServer server;
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service,"url","http://pricing"); ReflectionTestUtils.setField(service,"token","secret");
        server=MockRestServiceServer.createServer((RestTemplate)ReflectionTestUtils.getField(service,"http"));
    }
    @Test void missingOrderIdCannotReserveVoucher() {
        assertThrows(IllegalStateException.class, () -> service.redeem("OMNI8", 42L, null, new BigDecimal("144000")));
        verifyNoInteractions(jdbc);
        server.verify();
    }
    @Test void noVoucherDoesNotConsumeOrCallAnotherService() {
        assertEquals(BigDecimal.ZERO,service.redeem(null,1L,99L,new BigDecimal("120000"))); verifyNoInteractions(jdbc); server.verify();
    }
    @Test void redeemsNamespacedOrderAndUsesVerifiedDiscount() {
        server.expect(requestTo("http://pricing/api/v1/pricing/internal/vouchers/redeem"))
            .andExpect(header("X-Internal-Token","secret"))
            .andExpect(content().json("{\"code\":\"SAVE30\",\"orderId\":\"RIDE:99\",\"originalAmount\":120000}"))
            .andRespond(withSuccess("{\"valid\":true,\"discountAmount\":30000,\"finalAmount\":90000}",MediaType.APPLICATION_JSON));
        assertEquals(new BigDecimal("30000"),service.redeem(" save30 ",1L,99L,new BigDecimal("120000"))); server.verify();
    }
    @Test void invalidVoucherFailsCreationRatherThanChargingGross() {
        server.expect(requestTo("http://pricing/api/v1/pricing/internal/vouchers/redeem"))
            .andRespond(withSuccess("{\"valid\":false,\"message\":\"Expired\"}",MediaType.APPLICATION_JSON));
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.redeem("SAVE30",1L,99L,new BigDecimal("120000"))).getStatusCode().value());
    }
    @Test void unavailablePricingFailsClosedAndLeavesCompensationRecord() {
        server.expect(requestTo("http://pricing/api/v1/pricing/internal/vouchers/redeem")).andRespond(withServerError());
        assertEquals(503,assertThrows(ResponseStatusException.class,()->service.redeem("SAVE30",1L,99L,new BigDecimal("120000"))).getStatusCode().value());
        verify(jdbc).update(startsWith("insert into voucher_compensation"),eq(99L),eq("SAVE30"),eq(1L));
    }
    @Test void canceledOrderReleasesNamespacedVoucherButActiveOrderKeepsReservation() {
        when(jdbc.queryForList(startsWith("select * from voucher_compensation"))).thenReturn(List.of(Map.of("order_id",99L,"code","SAVE30","customer_id",1L)));
        when(jdbc.queryForObject(startsWith("select pg_try_advisory"),eq(Boolean.class),eq("VOUCHER_ORDER:99"))).thenReturn(true);
        when(jdbc.queryForList(startsWith("select status"),eq(99L))).thenReturn(List.of(Map.of("status","IN_PROGRESS")));
        service.compensateRolledBackOrders(); server.verify();
        verify(jdbc,never()).update(startsWith("delete from"),eq(99L));
        when(jdbc.queryForList(startsWith("select status"),eq(99L))).thenReturn(List.of(Map.of("status","CANCELLED")));
        server.expect(requestTo("http://pricing/api/v1/pricing/internal/vouchers/release"))
            .andExpect(content().json("{\"orderId\":\"RIDE:99\",\"code\":\"SAVE30\"}" )).andRespond(withSuccess());
        service.compensateRolledBackOrders(); server.verify();
        verify(jdbc).update(startsWith("delete from"),eq(99L));
    }
}
