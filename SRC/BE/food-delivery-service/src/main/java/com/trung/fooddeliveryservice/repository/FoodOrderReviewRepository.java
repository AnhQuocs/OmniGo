package com.trung.fooddeliveryservice.repository;

import com.trung.fooddeliveryservice.entity.FoodOrderReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodOrderReviewRepository extends JpaRepository<FoodOrderReview, Long> {

    Optional<FoodOrderReview> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    Page<FoodOrderReview> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    Page<FoodOrderReview> findByRestaurantIdAndRestaurantRatingOrderByCreatedAtDesc(Long restaurantId, Integer rating, Pageable pageable);

    long countByRestaurantId(Long restaurantId);

    long countByRestaurantIdAndRestaurantRating(Long restaurantId, Integer rating);

    @Query("SELECT COALESCE(AVG(r.restaurantRating), 5.0) FROM FoodOrderReview r WHERE r.restaurantId = :restaurantId")
    Double calculateAverageRating(@Param("restaurantId") Long restaurantId);
}
