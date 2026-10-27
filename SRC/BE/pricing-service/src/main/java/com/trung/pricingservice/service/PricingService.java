package com.trung.pricingservice.service;

import com.trung.pricingservice.dto.request.PricingConfigRequest;
import com.trung.pricingservice.dto.request.PricingRequest;
import com.trung.pricingservice.dto.response.PricingResponse;
import com.trung.pricingservice.entity.PricingConfig;

import com.trung.pricingservice.dto.request.DeliveryFeeRequest;
import com.trung.pricingservice.dto.response.DeliveryFeeResponse;

public interface PricingService {
    PricingResponse calculateDynamicPrice(PricingRequest request, int nearbyDriversCount);
    DeliveryFeeResponse calculateDeliveryFee(DeliveryFeeRequest request);
    PricingConfig updateBasePricingConfig(PricingConfigRequest request);
    PricingConfig getBasePricingConfig();
}