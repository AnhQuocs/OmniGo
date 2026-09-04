package com.trung.paymentservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        String phoneNumber = request.getHeader("X-User-Phone");

        if (request.getRequestURI().contains("/api/v1/payments/momo/ipn")
                || request.getRequestURI().contains("/api/v1/payments/momo/return")
                || request.getRequestURI().contains("/api/v1/payments/vnpay/return")
                || request.getRequestURI().contains("/cancel-pending")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (role != null && (userId != null || phoneNumber != null)) {
            try {
                String cleanRole = role.toUpperCase().replace("ROLE_", "");
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + cleanRole),
                        new SimpleGrantedAuthority(cleanRole)
                );

                String principal = (userId != null && !userId.isBlank()) ? userId : phoneNumber;

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.error("Lỗi khi thiết lập Header Authentication trong payment-service", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}