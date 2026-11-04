package com.trung.fooddeliveryservice.repository;

import com.trung.fooddeliveryservice.entity.FoodOrder;
import com.trung.fooddeliveryservice.util.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from FoodOrder o where o.id = :id")
    Optional<FoodOrder> findByIdWithLock(@Param("id") Long id);

    List<FoodOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<FoodOrder> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<FoodOrder> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<FoodOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<FoodOrder> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);

    List<FoodOrder> findAllByOrderByCreatedAtDesc();
}
