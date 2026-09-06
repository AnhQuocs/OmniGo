package com.trung.fooddeliveryservice.repository;

import com.trung.fooddeliveryservice.entity.FoodOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderItemRepository extends JpaRepository<FoodOrderItem, Long> {

    List<FoodOrderItem> findByFoodOrderId(Long foodOrderId);
}
