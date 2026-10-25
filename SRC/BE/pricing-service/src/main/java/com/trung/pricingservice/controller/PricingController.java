package com.trung.pricingservice.controller;

import com.trung.pricingservice.dto.google.RouteResult;
import com.trung.pricingservice.dto.request.DeliveryFeeRequest;
import com.trung.pricingservice.dto.request.PricingRequest;
import com.trung.pricingservice.dto.response.DeliveryFeeResponse;
import com.trung.pricingservice.dto.response.PricingResponse;
import com.trung.pricingservice.service.GoogleRoutesService;
import com.trung.pricingservice.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trung.pricingservice.dto.request.RouteRequest;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;
    private final GoogleRoutesService googleRoutesService;

    @PostMapping("/calculate")
    public ResponseEntity<PricingResponse> calculatePrice(
            @Valid @RequestBody PricingRequest request,
            @RequestParam(value = "nearbyDriversCount", defaultValue = "0") int nearbyDriversCount) {

        PricingResponse response = pricingService.calculateDynamicPrice(request, nearbyDriversCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delivery-fee")
    public ResponseEntity<DeliveryFeeResponse> calculateDeliveryFee(
            @Valid @RequestBody DeliveryFeeRequest request) {

        DeliveryFeeResponse response = pricingService.calculateDeliveryFee(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/route")
    public ResponseEntity<RouteResult> getRoute(@Valid @ModelAttribute RouteRequest request) {
        RouteResult result = googleRoutesService.getRouteInfo(
                request.getStartLatitude(),
                request.getStartLongitude(),
                request.getEndLatitude(),
                request.getEndLongitude(),
                request.getVehicleType()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResult> calculateRoute(@Valid @RequestBody RouteRequest request) {
        RouteResult result = googleRoutesService.getRouteInfo(
                request.getStartLatitude(),
                request.getStartLongitude(),
                request.getEndLatitude(),
                request.getEndLongitude(),
                request.getVehicleType()
        );
        return ResponseEntity.ok(result);
    }
}

