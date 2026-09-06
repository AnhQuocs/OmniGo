package com.trung.fooddeliveryservice.repository;

import com.trung.fooddeliveryservice.entity.FoodOrder;
import com.trung.fooddeliveryservice.util.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {

    List<FoodOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<FoodOrder> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<FoodOrder> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<FoodOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<FoodOrder> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);

    List<FoodOrder> findAllByOrderByCreatedAtDesc();
}
