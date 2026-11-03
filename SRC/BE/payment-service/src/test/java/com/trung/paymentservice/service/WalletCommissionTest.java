package com.trung.paymentservice.service;
import com.trung.paymentservice.service.impl.WalletServiceImpl;
import com.trung.paymentservice.repository.*;
import com.trung.paymentservice.entity.*;
import com.trung.paymentservice.dto.request.FoodOrderPayoutRequest;
import com.trung.paymentservice.util.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class WalletCommissionTest {
    @Test void onlinePaysBothPartiesAfterConfiguredFees() { verifyPayout("WALLET", "87500"); }
    @Test void cashOnlyDebitsRestaurantFee() { verifyPayout("CASH", "-12500"); }
    private void verifyPayout(String method, String expectedRestaurantBalance) {
        WalletRepository wallets = mock(WalletRepository.class);
        TransactionRepository transactions = mock(TransactionRepository.class);
        PlatformFeeConfigRepository configs = mock(PlatformFeeConfigRepository.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class, RETURNS_DEEP_STUBS);
        when(redis.opsForValue().get(PlatformFeeService.REDIS_KEY)).thenReturn("12.5");
        Wallet driver = Wallet.builder().id(1L).userId(11L).userType(UserType.DRIVER).balance(BigDecimal.ZERO).build();
        Wallet restaurant = Wallet.builder().id(2L).userId(22L).userType(UserType.RESTAURANT).balance(BigDecimal.ZERO).build();
        when(wallets.findByUserIdAndUserTypeWithLock(11L, UserType.DRIVER)).thenReturn(Optional.of(driver));
        when(wallets.findByUserIdAndUserTypeWithLock(22L, UserType.RESTAURANT)).thenReturn(Optional.of(restaurant));
        when(wallets.findByUserIdAndUserType(22L, UserType.RESTAURANT)).thenReturn(Optional.of(restaurant));
        List<Transaction> ledger = new ArrayList<>();
        when(transactions.save(any())).thenAnswer(inv -> { Transaction tx = inv.getArgument(0); ledger.add(tx); return tx; });
        when(transactions.findByOrderId(anyString())).thenAnswer(inv -> ledger.stream().filter(t -> t.getOrderId().equals(inv.getArgument(0))).findFirst());
        when(transactions.findByWalletIdOrderByCreatedAtDesc(1L)).thenAnswer(inv -> ledger.stream().filter(t -> t.getWalletId().equals(1L)).toList());
        WalletServiceImpl service = new WalletServiceImpl(new PlatformFeeService(configs, redis), wallets, transactions, mock(RestTemplate.class));
        FoodOrderPayoutRequest request = FoodOrderPayoutRequest.builder().driverId(11L).restaurantOwnerId(22L).orderId(99L)
                .foodAmount(new BigDecimal("100000")).deliveryFee(new BigDecimal("20000")).paymentMethod(method).build();
        service.processFoodOrderPayout(request);
        assertEquals(0, restaurant.getBalance().compareTo(new BigDecimal(expectedRestaurantBalance)));
        assertEquals(0, driver.getBalance().compareTo(new BigDecimal("17500")));
        BigDecimal totalFees = ledger.stream().filter(t -> t.getTransactionType() == TransactionType.COMMISSION_FEE)
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalFees.compareTo(new BigDecimal("15000")));
        int count = ledger.size();
        when(redis.opsForValue().get(PlatformFeeService.REDIS_KEY)).thenReturn("30");
        service.processFoodOrderPayout(request);
        assertEquals(count, ledger.size());
        assertEquals(0, restaurant.getBalance().compareTo(new BigDecimal(expectedRestaurantBalance)));
        assertEquals(0, driver.getBalance().compareTo(new BigDecimal("17500")));
    }
}
