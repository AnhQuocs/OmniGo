package com.trung.fooddeliveryservice.service;
import com.trung.fooddeliveryservice.dto.request.*;
import com.trung.fooddeliveryservice.entity.*;
import com.trung.fooddeliveryservice.repository.*;
import com.trung.fooddeliveryservice.mapper.FoodOrderMapper;
import com.trung.fooddeliveryservice.publisher.FoodEventPublisher;
import com.trung.fooddeliveryservice.service.impl.FoodOrderServiceImpl;
import com.trung.fooddeliveryservice.util.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class FoodVoucherOrderTest {
    @Test void cashAndWalletStoreDiscountedTotalWhilePreservingItemsAndDeliveryFee() throws Exception {
        for (String method:List.of("CASH","WALLET")) {
            var orders=mock(FoodOrderRepository.class); var restaurants=mock(RestaurantRepository.class); var menu=mock(MenuItemRepository.class);
            var vouchers=mock(OrderVoucherService.class); var publisher=mock(FoodEventPublisher.class);
            var service=new FoodOrderServiceImpl(mock(FinancialOutbox.class),new TransactionTemplate(mock(PlatformTransactionManager.class)),orders,
                    mock(FoodOrderItemRepository.class),restaurants,menu,new FoodOrderMapper(),publisher,mock(StringRedisTemplate.class),mock(RestTemplate.class));
            ReflectionTestUtils.setField(service,"orderVouchers",vouchers);
            var restaurant=Restaurant.builder().id(2L).name("Kitchen").status(RestaurantStatus.OPEN).build();
            when(restaurants.findById(2L)).thenReturn(Optional.of(restaurant));
            when(menu.findAllById(any())).thenReturn(List.of(MenuItem.builder().id(3L).restaurant(restaurant).price(new BigDecimal("50000")).build()));
            when(orders.saveAndFlush(any())).thenAnswer(i -> { FoodOrder o=i.getArgument(0); o.setId(99L); return o; });
            when(orders.save(any())).thenAnswer(i -> i.getArgument(0));
            when(vouchers.redeem("SAVE30",1L,99L,new BigDecimal("115000"))).thenReturn(new BigDecimal("30000"));
            var request=FoodOrderCreateRequest.builder().restaurantId(2L).customerName("Customer").customerPhone("0900000000")
                    .deliveryAddress("Address").paymentMethod(method).voucherCode("SAVE30")
                    .items(List.of(FoodOrderItemRequest.builder().menuItemId(3L).quantity(2).build())).build();
            var result=service.createOrder(request,1L);
            assertEquals(0,result.getTotalPrice().compareTo(new BigDecimal("85000")));
            assertEquals(0,result.getOriginalAmount().compareTo(new BigDecimal("115000")));
            assertEquals(0,result.getDeliveryFee().compareTo(new BigDecimal("15000")));
            assertEquals("SAVE30",result.getVoucherCode());
            assertEquals("CASH".equals(method) ? OrderStatus.PENDING : OrderStatus.AWAITING_PAYMENT,result.getStatus());
            verify(vouchers).redeem("SAVE30",1L,99L,new BigDecimal("115000"));
        }
    }
}
