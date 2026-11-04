package com.trung.fooddeliveryservice.filter;

import com.trung.fooddeliveryservice.security.JwtTokenProvider;
import com.trung.fooddeliveryservice.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeaderAndJwtAuthFilter extends OncePerRequestFilter {
    @Value("${app.internal-api-token:}")
    private String gatewayToken;
    private boolean trustedGateway(HttpServletRequest request) {
        String supplied = request.getHeader("X-Gateway-Token");
        return gatewayToken != null && !gatewayToken.isBlank() && supplied != null
                && MessageDigest.isEqual(gatewayToken.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }


    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String userIdStr = request.getHeader("X-User-Id");
            String roleStr = request.getHeader("X-User-Role");
            String phoneStr = request.getHeader("X-User-Phone");

            if (trustedGateway(request) && StringUtils.hasText(userIdStr) && StringUtils.hasText(roleStr)) {
                // 1. Authenticate via Gateway forwarded headers
                Long userId = Long.parseLong(userIdStr);
                String cleanRole = roleStr.toUpperCase().replace("ROLE_", "");
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + cleanRole),
                        new SimpleGrantedAuthority(cleanRole)
                );

                UserPrincipal principal = UserPrincipal.builder()
                        .userId(userId)
                        .phoneNumber(phoneStr)
                        .role(cleanRole)
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                // 2. Fallback: Authenticate via Bearer Token header
                String bearerToken = request.getHeader("Authorization");
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    if (jwtTokenProvider.validateToken(token)) {
                        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                        Object userIdObj = claims.get("userId");
                        Long userId = userIdObj != null ? Long.parseLong(userIdObj.toString()) : null;
                        String role = claims.get("role", String.class);
                        String phone = claims.getSubject();

                        if (role != null) {
                            String cleanRole = role.toUpperCase().replace("ROLE_", "");
                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority("ROLE_" + cleanRole),
                                    new SimpleGrantedAuthority(cleanRole)
                            );

                            UserPrincipal principal = UserPrincipal.builder()
                                    .userId(userId)
                                    .phoneNumber(phone)
                                    .role(cleanRole)
                                    .authorities(authorities)
                                    .build();

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    principal, null, authorities
                            );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi xác thực người dùng trong Filter: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
