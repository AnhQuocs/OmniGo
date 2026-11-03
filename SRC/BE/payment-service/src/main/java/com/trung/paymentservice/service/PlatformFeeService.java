package com.trung.paymentservice.service;
import com.trung.paymentservice.entity.PlatformFeeConfig;
import com.trung.paymentservice.repository.PlatformFeeConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
@Service @RequiredArgsConstructor
public class PlatformFeeService {
    private final PlatformFeeConfigRepository repository;
    public BigDecimal getPercentage() {
        return repository.findById(1L).orElseGet(PlatformFeeConfig::new).getPercentage();
    }
    @Transactional
    public BigDecimal update(BigDecimal percentage) {
        if (percentage == null || percentage.signum() < 0 || percentage.compareTo(new BigDecimal("100")) > 0
                || percentage.stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tỷ lệ sàn phải từ 0 đến 100, tối đa 2 chữ số thập phân");
        }
        PlatformFeeConfig config = new PlatformFeeConfig();
        config.setPercentage(percentage);
        return repository.save(config).getPercentage();
    }
    public BigDecimal calculate(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
    }
}
