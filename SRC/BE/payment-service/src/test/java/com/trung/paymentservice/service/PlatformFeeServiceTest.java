package com.trung.paymentservice.service;
import com.trung.paymentservice.repository.PlatformFeeConfigRepository;
import org.junit.jupiter.api.Test;
import com.trung.paymentservice.entity.PlatformFeeConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;
import java.util.Optional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PlatformFeeServiceTest {
    private final PlatformFeeConfigRepository repository = mock(PlatformFeeConfigRepository.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class, RETURNS_DEEP_STUBS);
    private final PlatformFeeService service = new PlatformFeeService(repository, redis);

    @Test void savesOnlyInRedisWithoutExpiration() {
        assertEquals(new BigDecimal("12.5"), service.update(new BigDecimal("12.5")));
        verify(redis.opsForValue()).set(PlatformFeeService.REDIS_KEY, "12.5");
        verifyNoInteractions(repository);
    }
    @Test void readsRedisInsteadOfSql() {
        when(redis.opsForValue().get(PlatformFeeService.REDIS_KEY)).thenReturn("15.25");
        assertEquals(new BigDecimal("15.25"), service.getPercentage());
        verifyNoInteractions(repository);
    }
    @Test void importsExistingSqlRateWithoutOverwritingConcurrentAdminUpdate() {
        PlatformFeeConfig old = new PlatformFeeConfig(); old.setPercentage(new BigDecimal("18"));
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        when(redis.opsForValue().get(PlatformFeeService.REDIS_KEY)).thenReturn(null, "25");
        assertEquals(new BigDecimal("25"), service.getPercentage());
        verify(redis.opsForValue()).setIfAbsent(PlatformFeeService.REDIS_KEY, "18");
    }
    @Test void initializesDefaultWhenNoPreviousSettingExists() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(redis.opsForValue().get(PlatformFeeService.REDIS_KEY)).thenReturn(null, "20.00");
        assertEquals(new BigDecimal("20.00"), service.getPercentage());
        verify(redis.opsForValue()).setIfAbsent(PlatformFeeService.REDIS_KEY, "20.00");
    }
    @Test void failsRatherThanChargingDefaultWhenRedisUnavailable() {
        when(redis.opsForValue().get(PlatformFeeService.REDIS_KEY)).thenThrow(new RedisConnectionFailureException("offline"));
        assertThrows(RedisConnectionFailureException.class, service::getPercentage);
        verifyNoInteractions(repository);
    }
    @Test void rejectsCorruptStoredRates() {
        for (String value : new String[]{"oops", "-1", "101", "1.234"}) {
            when(redis.opsForValue().get(PlatformFeeService.REDIS_KEY)).thenReturn(value);
            assertThrows(ResponseStatusException.class, service::getPercentage);
        }
    }
    @Test void computesConfiguredFeeAndRoundsDong() {
        assertEquals(new BigDecimal("12500"), service.calculate(new BigDecimal("100000"), new BigDecimal("12.5")));
        assertEquals(new BigDecimal("1"), service.calculate(new BigDecimal("5"), new BigDecimal("10")));
        assertEquals(new BigDecimal("0"), service.calculate(new BigDecimal("100000"), BigDecimal.ZERO));
        assertEquals(new BigDecimal("100000"), service.calculate(new BigDecimal("100000"), new BigDecimal("100")));
    }
    @Test void rejectsInvalidRates() {
        for (String value : new String[]{"-1", "100.01", "20.001"})
            assertThrows(ResponseStatusException.class, () -> service.update(new BigDecimal(value)));
        assertThrows(ResponseStatusException.class, () -> service.update(null));
        verifyNoInteractions(repository);
    }
}
