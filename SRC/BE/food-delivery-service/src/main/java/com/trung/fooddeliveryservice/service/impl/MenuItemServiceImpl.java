package com.trung.fooddeliveryservice.service.impl;

import com.trung.fooddeliveryservice.dto.request.MenuItemRequest;
import com.trung.fooddeliveryservice.dto.response.MenuItemResponse;
import com.trung.fooddeliveryservice.entity.MenuItem;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.mapper.MenuItemMapper;
import com.trung.fooddeliveryservice.repository.MenuItemRepository;
import com.trung.fooddeliveryservice.repository.RestaurantRepository;
import com.trung.fooddeliveryservice.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemMapper menuItemMapper;

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", key = "#restaurantId")
    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request, Long ownerId) throws ResourceNotFoundException, UnauthorizedException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng ID {} để thêm món", restaurantId);
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + restaurantId);
                });

        if (!restaurant.getOwnerId().equals(ownerId)) {
            log.warn("Người dùng ID {} không có quyền thêm món vào quán ID {}", ownerId, restaurantId);
            throw new UnauthorizedException("Bạn không có quyền thêm món ăn vào nhà hàng này");
        }

        MenuItem menuItem = menuItemMapper.toEntity(request, restaurant);
        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Thêm thành công món ăn '{}' (ID: {}) vào nhà hàng ID {}", saved.getName(), saved.getId(), restaurantId);
        return menuItemMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByRestaurantId(Long restaurantId) {
        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);
        return menuItemMapper.toResponseList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAvailableMenuItems(Long restaurantId) {
        List<MenuItem> items = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
        return menuItemMapper.toResponseList(items);
    }

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request, Long ownerId) throws ResourceNotFoundException, UnauthorizedException {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy món ăn ID {} để sửa", itemId);
                    return new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + itemId);
                });

        if (!menuItem.getRestaurant().getOwnerId().equals(ownerId)) {
            log.warn("Người dùng ID {} không có quyền sửa món ID {}", ownerId, itemId);
            throw new UnauthorizedException("Bạn không có quyền chỉnh sửa món ăn này");
        }

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setCategory(request.getCategory());
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }

        MenuItem updated = menuItemRepository.save(menuItem);
        log.info("Cập nhật thành công món ăn ID {}", updated.getId());
        return menuItemMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public void deleteMenuItem(Long itemId, Long ownerId) throws ResourceNotFoundException, UnauthorizedException {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy món ăn ID {} để xóa", itemId);
                    return new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + itemId);
                });

        if (!menuItem.getRestaurant().getOwnerId().equals(ownerId)) {
            log.warn("Người dùng ID {} không có quyền xóa món ID {}", ownerId, itemId);
            throw new UnauthorizedException("Bạn không có quyền xóa món ăn này");
        }

        menuItemRepository.delete(menuItem);
        log.info("Xóa thành công món ăn ID {}", itemId);
    }
}
