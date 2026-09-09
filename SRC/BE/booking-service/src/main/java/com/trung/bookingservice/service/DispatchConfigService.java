package com.trung.bookingservice.service;

import java.util.Map;

import com.trung.bookingservice.dto.request.DispatchConfigRequest;

public interface DispatchConfigService {

    Map<String, Double> getDispatchConfig();

    Map<String, Double> updateDispatchConfig(DispatchConfigRequest request);

    Double getMaxRideDistanceKm();
    
    Double getMaxFoodDeliveryDistanceKm();
}
