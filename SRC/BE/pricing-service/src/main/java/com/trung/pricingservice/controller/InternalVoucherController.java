package com.trung.pricingservice.controller;
import com.trung.pricingservice.service.VoucherService;
import com.trung.pricingservice.dto.request.CommitVoucherRequest;
import com.trung.pricingservice.dto.response.ApplyVoucherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/pricing/internal/vouchers")
public class InternalVoucherController {
    private final VoucherService vouchers;
    @Value("${app.internal-api-token:${INTERNAL_API_TOKEN:omnipay-internal-shared-secret-token-32bytes-min}}") private String token;
    private void authorize(String supplied) {
        if (token == null || token.isBlank() || supplied == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    @PostMapping("/redeem")
    public ApplyVoucherResponse redeem(@RequestHeader(value="X-Internal-Token",required=false) String supplied,
            @jakarta.validation.Valid @RequestBody CommitVoucherRequest request) {
        authorize(supplied);
        if (!Double.isFinite(request.getOriginalAmount()) || request.getOriginalAmount() <= 0 || request.getOrderId() == null ||
            !(request.getOrderId().startsWith("FOOD:") || request.getOrderId().startsWith("RIDE:")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return vouchers.commitVoucher(request);
    }
    @PostMapping("/release")
    public void release(@RequestHeader(value="X-Internal-Token",required=false) String supplied,
            @RequestBody java.util.Map<String,String> request) {
        authorize(supplied);
        vouchers.rollbackVoucherUsage(request.get("code"), request.get("userId"), request.get("orderId"));
    }
}
