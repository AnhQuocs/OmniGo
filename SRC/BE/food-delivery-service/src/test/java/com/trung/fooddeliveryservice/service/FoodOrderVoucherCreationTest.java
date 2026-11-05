package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.request.*;
import com.trung.fooddeliveryservice.entity.*;
import com.trung.fooddeliveryservice.mapper.FoodOrderMapper;
import com.trung.fooddeliveryservice.publisher.FoodEventPublisher;
import com.trung.fooddeliveryservice.repository.*;
import com.trung.fooddeliveryservice.service.impl.FoodOrderServiceImpl;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionCallback;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FoodOrderVoucherCreationTest {
    @Test void usesPersistedCopyIdAndChargesDiscountOnce() throws Exception {
        var orders = mock(FoodOrderRepository.class);
        var restaurants = mock(RestaurantRepository.class);
        var menus = mock(MenuItemRepository.class);
        var vouchers = mock(OrderVoucherService.class);
        var tx = mock(TransactionTemplate.class);
        when(tx.execute(any())).thenAnswer(call -> ((TransactionCallback<?>) call.getArgument(0)).doInTransaction(null));
        var service = new FoodOrderServiceImpl(null, tx, orders, null, restaurants, menus,
                mock(FoodOrderMapper.class), mock(FoodEventPublisher.class), null, null);
        ReflectionTestUtils.setField(service, "orderVouchers", vouchers);
        var restaurant = new Restaurant(); restaurant.setId(1L); restaurant.setStatus(RestaurantStatus.OPEN);
        when(restaurants.findById(1L)).thenReturn(Optional.of(restaurant));
        var item = new MenuItem(); item.setId(2L); item.setRestaurant(restaurant);
        item.setPrice(new BigDecimal("65000")); item.setIsAvailable(true);
        when(menus.findAllById(any())).thenReturn(List.of(item));
        when(orders.saveAndFlush(any())).thenAnswer(call -> {
            FoodOrder original = call.getArgument(0);
            var managed = new FoodOrder(); BeanUtils.copyProperties(original, managed);
            managed.setId(99L);
            assertNull(original.getId()); // merge may return a distinct managed instance
            return managed;
        });
        when(vouchers.redeem("OMNI8", 42L, 99L, new BigDecimal("80000"))).thenReturn(new BigDecimal("6400"));
        when(orders.save(any())).thenAnswer(call -> call.getArgument(0));
        var request = FoodOrderCreateRequest.builder().restaurantId(1L).customerName("Test")
                .customerPhone("0900000000").deliveryAddress("Test").paymentMethod("WALLET")
                .voucherCode("OMNI8").items(List.of(FoodOrderItemRequest.builder().menuItemId(2L).quantity(1).build())).build();
        service.createOrder(request, 42L);
        verify(vouchers).redeem("OMNI8", 42L, 99L, new BigDecimal("80000"));
        verify(orders).save(argThat(order -> order.getId().equals(99L)
                && order.getTotalPrice().compareTo(new BigDecimal("73600")) == 0
                && order.getDeliveryFee().compareTo(new BigDecimal("15000")) == 0));
    }
}
