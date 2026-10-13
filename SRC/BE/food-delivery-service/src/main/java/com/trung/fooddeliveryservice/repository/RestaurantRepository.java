package com.trung.fooddeliveryservice.repository;

import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.repository.projection.RestaurantNearbyProjection;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByOwnerId(Long ownerId);

    List<Restaurant> findByStatus(RestaurantStatus status);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Restaurant> searchByNameOrAddress(@Param("keyword") String keyword);

    @Query(value = "SELECT nearby.* FROM (SELECT r.id AS id, r.owner_id AS owner_id, r.name AS name, r.phone AS phone, " +
            "r.address AS address, r.latitude AS latitude, r.longitude AS longitude, " +
            "r.image_url AS image_url, r.status AS status, r.open_time AS open_time, " +
            "r.close_time AS close_time, r.rating AS rating, r.review_count AS review_count, " +
            "(6371.0 * 2.0 * ASIN(SQRT( " +
            "LEAST(1.0, GREATEST(0.0, " +
            "POWER(SIN(RADIANS(r.latitude - :latitude) / 2.0), 2) + " +
            "COS(RADIANS(:latitude)) * COS(RADIANS(r.latitude)) * " +
            "POWER(SIN(RADIANS(r.longitude - :longitude) / 2.0), 2) " +
            ")) " +
            "))) AS distance_km " +
            "FROM restaurants r " +
            "WHERE (r.is_locked = FALSE OR r.is_locked IS NULL) " +
            "AND r.status = 'OPEN' " +
            "AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL " +
            ") nearby WHERE nearby.distance_km <= :radiusKm " +
            "ORDER BY nearby.distance_km ASC, nearby.id ASC",
            countQuery = "SELECT COUNT(*) FROM (SELECT r.id, " +
            "(6371.0 * 2.0 * ASIN(SQRT( " +
            "LEAST(1.0, GREATEST(0.0, " +
            "POWER(SIN(RADIANS(r.latitude - :latitude) / 2.0), 2) + " +
            "COS(RADIANS(:latitude)) * COS(RADIANS(r.latitude)) * " +
            "POWER(SIN(RADIANS(r.longitude - :longitude) / 2.0), 2) " +
            ")) " +
            "))) AS distance_km " +
            "FROM restaurants r " +
            "WHERE (r.is_locked = FALSE OR r.is_locked IS NULL) " +
            "AND r.status = 'OPEN' " +
            "AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL " +
            ") nearby WHERE nearby.distance_km <= :radiusKm",
            nativeQuery = true)
    Page<RestaurantNearbyProjection> findNearbyRestaurants(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm,
            Pageable pageable);

    @Query(value = "SELECT nearby.* FROM (SELECT r.id AS id, r.owner_id AS owner_id, r.name AS name, r.phone AS phone, " +
            "r.address AS address, r.latitude AS latitude, r.longitude AS longitude, " +
            "r.image_url AS image_url, r.status AS status, r.open_time AS open_time, " +
            "r.close_time AS close_time, r.rating AS rating, r.review_count AS review_count, " +
            "(6371.0 * 2.0 * ASIN(SQRT( " +
            "LEAST(1.0, GREATEST(0.0, " +
            "POWER(SIN(RADIANS(r.latitude - :latitude) / 2.0), 2) + " +
            "COS(RADIANS(:latitude)) * COS(RADIANS(r.latitude)) * " +
            "POWER(SIN(RADIANS(r.longitude - :longitude) / 2.0), 2) " +
            ")) " +
            "))) AS distance_km " +
            "FROM restaurants r " +
            "WHERE (r.is_locked = FALSE OR r.is_locked IS NULL) " +
            "AND r.status = 'OPEN' " +
            "AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL " +
            ") nearby WHERE nearby.distance_km <= :radiusKm " +
            "ORDER BY nearby.distance_km ASC, nearby.id ASC", nativeQuery = true)
    List<RestaurantNearbyProjection> findNearbyRestaurants(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm);
}
