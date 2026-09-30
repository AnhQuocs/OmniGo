package com.trung.fooddeliveryservice.repository;

import com.trung.fooddeliveryservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
}
