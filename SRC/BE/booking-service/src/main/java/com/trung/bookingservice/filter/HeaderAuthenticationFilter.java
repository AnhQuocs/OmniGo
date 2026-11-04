package com.trung.bookingservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    @Value("${app.internal-api-token:${INTERNAL_API_TOKEN:omnipay-internal-shared-secret-token-32bytes-min}}")
    private String gatewayToken;

    private boolean trustedGateway(HttpServletRequest request) {
        String supplied = request.getHeader("X-Gateway-Token");
        return gatewayToken != null && !gatewayToken.isBlank() && supplied != null
                && MessageDigest.isEqual(gatewayToken.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String phoneNumber = request.getHeader("X-User-Phone");
        String role = request.getHeader("X-User-Role");
        String userId = request.getHeader("X-User-Id");

        if (trustedGateway(request) && role != null && (userId != null || phoneNumber != null)) {
            try {
                String cleanRole = role.toUpperCase().replace("ROLE_", "");
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + cleanRole),
                        new SimpleGrantedAuthority(cleanRole)
                );

                String principal = (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber : userId;

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Đã xác thực request từ Gateway cho User: {}, Role: {}", principal, cleanRole);
            } catch (Exception e) {
                log.error("Lỗi khi thiết lập Header Authentication trong booking-service", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
