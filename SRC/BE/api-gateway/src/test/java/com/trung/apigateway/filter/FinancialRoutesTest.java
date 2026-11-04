package com.trung.apigateway.filter;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class FinancialRoutesTest {
    @Test void blocksAllInternalFinancialRoutesBeforeAuthenticationBypass() {
        for(String path : new String[]{"/api/v1/payments/internal/refund-order","/api/v1/payments/internal/reconcile/food/1","/api/v1/internal/financial/orders/1","/api/v1/food-orders/1/paid","/api/v1/payments/booking/1/cancel-pending"}) {
            var exchange=MockServerWebExchange.from(MockServerHttpRequest.post(path).build());
            GatewayFilterChain chain=mock(GatewayFilterChain.class);
            new InternalBlockGlobalFilter().filter(exchange,chain).block();
            assertEquals(403,exchange.getResponse().getStatusCode().value());verifyNoInteractions(chain);
        }
    }
}
