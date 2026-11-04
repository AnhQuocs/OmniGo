package com.trung.paymentservice.service;
import com.trung.paymentservice.entity.PlatformFeeConfig;
import com.trung.paymentservice.repository.PlatformFeeConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
@Service @RequiredArgsConstructor
public class PlatformFeeService {
    private final PlatformFeeConfigRepository repository;
    static final String REDIS_KEY = "system:config:platform-fee:percentage";
    private final StringRedisTemplate redisTemplate;

    public BigDecimal getPercentage() {
        String value = redisTemplate.opsForValue().get(REDIS_KEY);
        if (value == null) {
            // Import the previous SQL setting only when Redis has not been initialized.
            BigDecimal previous = repository.findById(1L).orElseGet(PlatformFeeConfig::new).getPercentage();
            redisTemplate.opsForValue().setIfAbsent(REDIS_KEY, previous.toPlainString());
            // Read the winner: an admin may have saved a newer rate concurrently.
            value = redisTemplate.opsForValue().get(REDIS_KEY);
        }
        try {
            BigDecimal percentage = new BigDecimal(value);
            if (!isValid(percentage)) throw new IllegalArgumentException("Invalid percentage");
            return percentage;
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Tỷ lệ sàn trong Redis không hợp lệ", error);
        }
    }

    private boolean isValid(BigDecimal percentage) {
        return percentage != null && percentage.signum() >= 0
                && percentage.compareTo(new BigDecimal("100")) <= 0
                && percentage.stripTrailingZeros().scale() <= 2;
    }

    public BigDecimal update(BigDecimal percentage) {
        if (!isValid(percentage)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tỷ lệ sàn phải từ 0 đến 100, tối đa 2 chữ số thập phân");
        }
        redisTemplate.opsForValue().set(REDIS_KEY, percentage.toPlainString());
        return percentage;
    }
    public BigDecimal calculate(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
    }
}
