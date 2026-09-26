package com.trung.fooddeliveryservice.repository.projection;

public interface RestaurantNearbyProjection {
    Long getId();
    Long getOwnerId();
    String getName();
    String getPhone();
    String getAddress();
    Double getLatitude();
    Double getLongitude();
    String getImageUrl();
    String getStatus();
    String getOpenTime();
    String getCloseTime();
    Double getRating();
    Integer getReviewCount();
    Double getDistanceKm();
}
