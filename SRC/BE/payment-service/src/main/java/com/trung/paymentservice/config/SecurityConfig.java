package com.trung.paymentservice.config;

import com.trung.paymentservice.filter.GatewayAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayAuthenticationFilter gatewayAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                         .requestMatchers("/api/v1/payments/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/payments/wallet/deposit").denyAll()
                        .requestMatchers("/api/v1/payments/driver/withdraw").hasRole("DRIVER")
                        .requestMatchers("/api/v1/payments/internal/**", "/api/v1/payments/booking/*/cancel-pending", "/api/v1/payments/platform-fee").permitAll()
                        .requestMatchers("/actuator/health/**", "/error").permitAll()
                        .requestMatchers("/api/v1/payments/momo/ipn",
                                "/api/v1/payments/momo/return",
                                "/api/v1/payments/vnpay/return",
                                "/api/v1/payments/vnpay/ipn").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}