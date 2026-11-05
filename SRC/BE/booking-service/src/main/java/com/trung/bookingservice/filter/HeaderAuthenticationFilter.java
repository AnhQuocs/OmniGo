package com.trung.bookingservice.filter;

import com.trung.bookingservice.security.JwtTokenProvider;
import com.trung.bookingservice.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    @Value("${app.internal-api-token:${INTERNAL_API_TOKEN:omnipay-internal-shared-secret-token-32bytes-min}}")
    private String gatewayToken;

    private final JwtTokenProvider jwtTokenProvider;

    private boolean trustedGateway(HttpServletRequest request) {
        String supplied = request.getHeader("X-Gateway-Token");
        return gatewayToken != null && !gatewayToken.isBlank() && supplied != null
                && MessageDigest.isEqual(gatewayToken.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String phoneNumber = request.getHeader("X-User-Phone");
        String role = request.getHeader("X-User-Role");
        String userIdStr = request.getHeader("X-User-Id");

        try {
            if (trustedGateway(request) && role != null && (userIdStr != null || phoneNumber != null)) {
                Long userId = null;
                if (userIdStr != null && !userIdStr.isBlank()) {
                    try {
                        userId = Long.parseLong(userIdStr);
                    } catch (NumberFormatException ignored) {}
                }

                String cleanRole = role.toUpperCase().replace("ROLE_", "");
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + cleanRole),
                        new SimpleGrantedAuthority(cleanRole)
                );

                UserPrincipal principal = UserPrincipal.builder()
                        .userId(userId)
                        .phoneNumber(phoneNumber)
                        .role(cleanRole)
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Đã xác thực request từ Gateway cho User: {}, Role: {}", principal.getUsername(), cleanRole);
            } else {
                // Fallback: Validate Bearer token directly
                String bearerToken = request.getHeader("Authorization");
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    if (jwtTokenProvider.validateToken(token)) {
                        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                        Object userIdObj = claims.get("userId");
                        Long userId = null;
                        if (userIdObj != null) {
                            try {
                                userId = Long.parseLong(userIdObj.toString());
                            } catch (NumberFormatException ignored) {}
                        }
                        String tokenRole = claims.get("role", String.class);
                        String tokenPhone = claims.getSubject();

                        if (tokenRole != null) {
                            String cleanRole = tokenRole.toUpperCase().replace("ROLE_", "");
                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority("ROLE_" + cleanRole),
                                    new SimpleGrantedAuthority(cleanRole)
                            );

                            UserPrincipal principal = UserPrincipal.builder()
                                    .userId(userId)
                                    .phoneNumber(tokenPhone)
                                    .role(cleanRole)
                                    .authorities(authorities)
                                    .build();

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    authorities
                            );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.debug("Đã xác thực fallback Bearer Token cho User: {}, Role: {}", principal.getUsername(), cleanRole);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi thiết lập Header/JWT Authentication trong booking-service", e);
        }

        filterChain.doFilter(request, response);
    }
}
