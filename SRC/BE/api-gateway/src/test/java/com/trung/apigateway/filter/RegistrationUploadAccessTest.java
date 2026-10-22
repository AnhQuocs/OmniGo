package com.trung.apigateway.filter;

import com.trung.apigateway.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationUploadAccessTest {
    private final JwtUtil jwt = mock(JwtUtil.class);
    private final ReactiveStringRedisTemplate redis = mock(ReactiveStringRedisTemplate.class);
    private final AuthenticationGlobalFilter filter = new AuthenticationGlobalFilter(jwt, redis);

    @Test
    void allowsMultipartUploadBeforeDriverAndPartnerRegistration() {
        for (String route : new String[]{"/api/v1/drivers/upload-document", "/api/v1/restaurants/upload-image"}) {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.post(route)
                    .header("Content-Type", "multipart/form-data; boundary=test").build());
            GatewayFilterChain chain = mock(GatewayFilterChain.class);
            when(chain.filter(exchange)).thenReturn(Mono.empty());
            filter.filter(exchange, chain).block();
            verify(chain).filter(exchange);
        }
        verifyNoInteractions(jwt, redis);
    }

    @Test
    void reviewUploadStillRequiresAuthentication() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/food-reviews/upload-image").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        filter.filter(exchange, chain).block();
        assertEquals(401, exchange.getResponse().getStatusCode().value());
        verifyNoInteractions(chain);
    }

    @Test
    void uploadExemptionDoesNotAllowOtherDriverMethods() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.put("/api/v1/drivers/upload-document").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        filter.filter(exchange, chain).block();
        assertEquals(401, exchange.getResponse().getStatusCode().value());
        verifyNoInteractions(chain);
    }
}
