package com.trung.paymentservice.filter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component @Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FinancialInternalAuthFilter extends OncePerRequestFilter {
    @Value("${app.internal-api-token:}") private String token;
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean guarded = path.startsWith("/api/v1/payments/internal/")
                || path.startsWith("/api/v1/internal/financial/")
                || path.matches("/api/v1/food-orders/[^/]+/paid")
                || path.matches("/api/v1/payments/booking/[^/]+/cancel-pending");
        if (guarded) {
            String supplied = request.getHeader("X-Internal-Token");
            if (token == null || token.isBlank() || supplied == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(403); return;
            }
        }
        chain.doFilter(request, response);
    }
}
