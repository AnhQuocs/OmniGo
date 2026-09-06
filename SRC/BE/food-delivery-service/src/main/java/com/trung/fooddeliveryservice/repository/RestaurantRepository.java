package com.trung.fooddeliveryservice.repository;

import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
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
}
