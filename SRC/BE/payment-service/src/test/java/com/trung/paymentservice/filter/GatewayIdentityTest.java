package com.trung.paymentservice.filter;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class GatewayIdentityTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void rejectsForgedIdentityAndAcceptsGatewayStampedIdentity() throws Exception {
        var filter=new GatewayAuthenticationFilter();ReflectionTestUtils.setField(filter,"gatewayToken","test-token");
        var request=new MockHttpServletRequest("GET","/api/v1/payments/wallet");
        request.addHeader("X-User-Id","1");request.addHeader("X-User-Role","ADMIN");
        filter.doFilter(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        request.addHeader("X-Gateway-Token","test-token");
        filter.doFilter(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
