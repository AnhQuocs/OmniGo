package com.trung.pricingservice.service;
import com.trung.pricingservice.service.impl.VoucherServiceImpl;
import com.trung.pricingservice.repository.*;
import com.trung.pricingservice.mapper.VoucherMapper;
import com.trung.pricingservice.entity.*;
import com.trung.pricingservice.dto.request.*;
import com.trung.pricingservice.util.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class VoucherRedemptionTest {
    VoucherRepository db=mock(VoucherRepository.class);
    VoucherRedisRepository redis=mock(VoucherRedisRepository.class);
    VoucherUsageRepository usages=mock(VoucherUsageRepository.class);
    VoucherServiceImpl service=new VoucherServiceImpl(db,redis,usages,mock(VoucherMapper.class));
    Voucher voucher=Voucher.builder().code("SAVE30").discountType(DiscountType.FIXED_AMOUNT).discountValue(30000)
            .expireAt(LocalDateTime.now().plusDays(1)).startAt(LocalDateTime.now().minusDays(1)).build();
    @BeforeEach void setup() {
        EntityManager em=mock(EntityManager.class); Query query=mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query); when(query.setParameter(anyString(),any())).thenReturn(query);
        ReflectionTestUtils.setField(service,"entityManager",em);
        when(redis.findByCode("SAVE30")).thenReturn(Optional.of(voucher)); when(db.findByCode("SAVE30")).thenReturn(Optional.of(voucher));
        when(redis.getRemainingStock("SAVE30")).thenReturn(10); when(redis.decrementStock("SAVE30")).thenReturn(true);
        TransactionSynchronizationManager.initSynchronization();
    }
    @AfterEach void clear() { TransactionSynchronizationManager.clearSynchronization(); }
    CommitVoucherRequest request() { return CommitVoucherRequest.builder().code("SAVE30").userId("1").orderId("FOOD:35")
            .serviceType("FOOD_DELIVERY").originalAmount(120000).discountAmount(999999).finalAmount(1).build(); }
    @Test void recalculatesTotalsAndIgnoresClientDiscount() {
        var result=service.commitVoucher(request()); assertTrue(result.isValid()); assertEquals(30000,result.getDiscountAmount()); assertEquals(90000,result.getFinalAmount());
        verify(usages).save(argThat(u -> u.getFinalAmount()==90000 && u.getOriginalAmount()==120000));
    }
    @Test void retryReturnsPersistedDiscountWithoutConsumingAgain() {
        when(usages.findByVoucherCodeAndOrderId("SAVE30","FOOD:35")).thenReturn(Optional.of(VoucherUsage.builder()
            .voucherCode("SAVE30").userId("1").orderId("FOOD:35").serviceType("FOOD_DELIVERY").status(VoucherUsageStatus.USED)
            .originalAmount(120000).discountAmount(30000).finalAmount(90000).build()));
        assertEquals(90000,service.commitVoucher(request()).getFinalAmount()); verify(redis,never()).decrementStock(anyString());
    }
    @Test void wrongServiceAndExpiredVoucherCannotBeRedeemed() {
        voucher.setApplicableService(ApplicableService.RIDE_HAILING); assertFalse(service.commitVoucher(request()).isValid());
        voucher.setApplicableService(ApplicableService.ALL); voucher.setExpireAt(LocalDateTime.now().minusDays(1));
        assertFalse(service.commitVoucher(request()).isValid()); verify(usages,never()).save(any());
    }
    @Test void discountNeverExceedsOrderAndMissingRollbackDoesNotReleaseAnotherOrder() {
        voucher.setDiscountValue(999999); assertEquals(0,service.commitVoucher(request()).getFinalAmount());
        service.rollbackVoucherUsage("SAVE30","1","FOOD:missing"); verify(redis,never()).incrementStock(anyString());
        verify(usages,never()).findFirstByVoucherCodeAndUserIdAndStatusOrderByUsedAtDesc(any(),any(),any());
    }
}
