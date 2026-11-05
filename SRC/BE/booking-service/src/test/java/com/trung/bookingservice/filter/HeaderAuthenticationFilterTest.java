package com.trung.bookingservice.filter;

import com.trung.bookingservice.security.JwtTokenProvider;
import com.trung.bookingservice.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeaderAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private HeaderAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HeaderAuthenticationFilter(jwtTokenProvider);
        ReflectionTestUtils.setField(filter, "gatewayToken", "test-gateway-secret");
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesViaTrustedGatewayHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Gateway-Token", "test-gateway-secret");
        request.addHeader("X-User-Id", "42");
        request.addHeader("X-User-Role", "CUSTOMER");
        request.addHeader("X-User-Phone", "0987654321");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        assertInstanceOf(UserPrincipal.class, auth.getPrincipal());
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        assertEquals(42L, principal.getUserId());
        assertEquals("CUSTOMER", principal.getRole());
    }

    @Test
    void fallsBackToBearerTokenWhenGatewayTokenMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtTokenProvider.getClaimsFromToken("valid-jwt-token")).thenReturn(claims);
        when(claims.get("userId")).thenReturn("42");
        when(claims.get("role", String.class)).thenReturn("CUSTOMER");
        when(claims.getSubject()).thenReturn("0987654321");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        assertInstanceOf(UserPrincipal.class, auth.getPrincipal());
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        assertEquals(42L, principal.getUserId());
        assertEquals("CUSTOMER", principal.getRole());
    }

    @Test
    void leavesContextUnauthenticatedWhenNoValidCredentials() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

