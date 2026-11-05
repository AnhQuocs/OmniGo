package com.trung.pricingservice.controller;

import com.trung.pricingservice.dto.request.CommitVoucherRequest;
import com.trung.pricingservice.dto.response.ApplyVoucherResponse;
import com.trung.pricingservice.exception.GlobalExceptionHandler;
import com.trung.pricingservice.service.VoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalVoucherControllerTest {

    private final VoucherService voucherService = mock(VoucherService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        InternalVoucherController controller = new InternalVoucherController(voucherService);
        ReflectionTestUtils.setField(controller, "token", "test-internal-token");
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void redeemWithValidPayloadOmittingDiscountAndFinalAmountSucceeds() throws Exception {
        when(voucherService.commitVoucher(any(CommitVoucherRequest.class))).thenReturn(
                ApplyVoucherResponse.builder()
                        .valid(true)
                        .message("Voucher applied")
                        .discountAmount(30000.0)
                        .finalAmount(70000.0)
                        .build()
        );

        String body = """
                {
                    "code": "SUMMER",
                    "userId": "1",
                    "orderId": "RIDE:12",
                    "serviceType": "RIDE_HAILING",
                    "originalAmount": 100000.0
                }
                """;

        mockMvc.perform(post("/api/v1/pricing/internal/vouchers/redeem")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(30000.0))
                .andExpect(jsonPath("$.finalAmount").value(70000.0));
    }

    @Test
    void redeemWithoutTokenReturnsForbidden() throws Exception {
        String body = """
                {
                    "code": "SUMMER",
                    "userId": "1",
                    "orderId": "RIDE:12",
                    "serviceType": "RIDE_HAILING",
                    "originalAmount": 100000.0
                }
                """;

        mockMvc.perform(post("/api/v1/pricing/internal/vouchers/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void redeemWithNullOriginalAmountReturnsBadRequest() throws Exception {
        String body = """
                {
                    "code": "SUMMER",
                    "userId": "1",
                    "orderId": "RIDE:12",
                    "serviceType": "RIDE_HAILING",
                    "originalAmount": null
                }
                """;

        mockMvc.perform(post("/api/v1/pricing/internal/vouchers/redeem")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}

