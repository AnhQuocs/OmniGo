package com.trung.bookingservice.filter;

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
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String phoneNumber = request.getHeader("X-User-Phone");
        String role = request.getHeader("X-User-Role");
        String userId = request.getHeader("X-User-Id");

        if (role != null && (userId != null || phoneNumber != null)) {
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