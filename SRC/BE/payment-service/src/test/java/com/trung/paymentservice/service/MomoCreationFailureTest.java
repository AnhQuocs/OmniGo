package com.trung.paymentservice.service;
import com.trung.paymentservice.service.impl.MomoService;
import com.trung.paymentservice.config.MomoConfig;
import com.trung.paymentservice.dto.request.MomoCreateRequest;
import com.trung.paymentservice.dto.response.MomoCreateResponse;
import com.trung.paymentservice.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class MomoCreationFailureTest {
    @Test void rejectedCreationIsPersistedButAmbiguousOrDuplicateResultIsNot() {
        for (int code : new int[]{21,1001,1006,40,41,43,1000,9000,99}) {
            var pending = mock(PendingPaymentStore.class);
            var http = mock(RestTemplate.class);
            var config = new MomoConfig(); config.setPartnerCode("TEST"); config.setSecretKey("secret"); config.setEndpoint("https://test.invalid");
            when(http.postForEntity(any(URI.class), any(HttpEntity.class), eq(MomoCreateResponse.class))).thenAnswer(call -> {
                var request = (MomoCreateRequest)((HttpEntity<?>)call.getArgument(1)).getBody();
                var response = new MomoCreateResponse(); response.setOrderId(request.getOrderId()); response.setPartnerCode("TEST"); response.setResultCode(code);
                return ResponseEntity.ok(response);
            });
            var service = new MomoService(pending,mock(PaymentWorkflowService.class),config,new MomoEncoder(),mock(TransactionRepository.class),mock(WalletRepository.class),mock(WalletService.class),http);
            var error = assertThrows(ResponseStatusException.class, () -> service.createPayment(1L,null,2L,BigDecimal.TEN,"FOOD_PAYMENT"));
            if (code == 21 || code == 1001 || code == 1006) {
                assertEquals(422,error.getStatusCode().value()); verify(pending).rejectCreation(anyString());
            } else {
                assertEquals(502,error.getStatusCode().value()); verify(pending,never()).rejectCreation(anyString());
            }
        }
    }
}
