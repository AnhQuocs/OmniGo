package com.trung.bookingservice.filter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.servlet.FilterChain;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class FinancialInternalAuthFilterTest {
    @Test void rejectsAbsentAndInvalidTokensEvenOnDirectRequests() throws Exception {
        for(String path : new String[]{"/api/v1/payments/internal/refund-order","/api/v1/payments/internal/reconcile/food/1","/api/v1/internal/financial/orders/1","/api/v1/food-orders/1/paid"}) {
            var filter=new FinancialInternalAuthFilter(); ReflectionTestUtils.setField(filter,"token","secret");
            var req=new MockHttpServletRequest("POST",path);req.addHeader("X-Internal-Token","wrong");
            var res=new MockHttpServletResponse();FilterChain chain=mock(FilterChain.class);
            filter.doFilter(req,res,chain);assertEquals(403,res.getStatus());verifyNoInteractions(chain);
        }
    }
    @Test void acceptsMatchingTokenAndFailsClosedWhenUnconfigured() throws Exception {
        var filter=new FinancialInternalAuthFilter();ReflectionTestUtils.setField(filter,"token","secret");
        var req=new MockHttpServletRequest("POST","/api/v1/payments/internal/refund-order");req.addHeader("X-Internal-Token","secret");
        var res=new MockHttpServletResponse();FilterChain chain=mock(FilterChain.class);
        filter.doFilter(req,res,chain);verify(chain).doFilter(req,res);
        ReflectionTestUtils.setField(filter,"token","");reset(chain);
        filter.doFilter(req,res,chain);assertEquals(403,res.getStatus());verifyNoInteractions(chain);
    }
}
