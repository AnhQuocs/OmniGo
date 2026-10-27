package com.trung.pricingservice.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trung.pricingservice.entity.Voucher;
import com.trung.pricingservice.util.enums.VoucherStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VoucherRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX_INFO = "pricing:voucher:info:";
    private static final String KEY_PREFIX_STOCK = "pricing:voucher:stock:";
    private static final String KEY_PREFIX_USER_USAGE = "pricing:voucher:user_usage:";
    private static final String KEY_ALL_CODES = "pricing:voucher:all_codes";

    // Lua script: Kiểm tra tồn kho > 0 và trừ số lượng trong 1 lệnh duy nhất (Atomic)
    // Nếu tồn kho < 0 (giá trị -1 đại diện cho UNLIMITED) thì thành công luôn và không trừ!
    private static final String LUA_DECREMENT_STOCK =
            "local stock = redis.call('get', KEYS[1])\n" +
            "if not stock then\n" +
            "    return 0\n" +
            "end\n" +
            "local numStock = tonumber(stock)\n" +
            "if numStock < 0 then\n" +
            "    return 1\n" +
            "end\n" +
            "if numStock == 0 then\n" +
            "    return 0\n" +
            "end\n" +
            "redis.call('decr', KEYS[1])\n" +
            "return 1\n";

    private final DefaultRedisScript<Long> decrementScript = new DefaultRedisScript<>(LUA_DECREMENT_STOCK, Long.class);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String getInfoKey(String code) {
        return KEY_PREFIX_INFO + code.trim().toUpperCase();
    }

    private String getStockKey(String code) {
        return KEY_PREFIX_STOCK + code.trim().toUpperCase();
    }

    private String getUserUsageKey(String code) {
        return KEY_PREFIX_USER_USAGE + code.trim().toUpperCase();
    }

    public void saveVoucher(Voucher voucher) {
        String code = voucher.getCode().trim().toUpperCase();
        String infoKey = getInfoKey(code);
        String stockKey = getStockKey(code);
        String userUsageKey = getUserUsageKey(code);

        LocalDateTime now = LocalDateTime.now();
        Duration ttl = Duration.between(now, voucher.getExpireAt());

        if (ttl.isNegative() || ttl.isZero()) {
            // Đã hết hạn, không lưu
            log.warn("Voucher {} đã hết hạn, bỏ qua lưu Redis", code);
            return;
        }

        // Lưu thông tin voucher
        redisTemplate.opsForValue().set(infoKey, voucher, ttl);

        // Lưu stock (-1 nếu không giới hạn)
        if (voucher.getTotalUsageLimit() <= 0) {
            redisTemplate.opsForValue().set(stockKey, -1, ttl);
        } else {
            int remainingStock = voucher.getTotalUsageLimit() - voucher.getUsedCount();
            redisTemplate.opsForValue().set(stockKey, Math.max(0, remainingStock), ttl);
        }

        // Đặt TTL cho user usage key nếu key đó tồn tại
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userUsageKey))) {
            redisTemplate.expire(userUsageKey, ttl);
        }

        // Thêm vào danh mục tất cả mã
        redisTemplate.opsForSet().add(KEY_ALL_CODES, code);
        log.info("Đã lưu voucher {} vào Redis thành công, TTL: {} phút", code, ttl.toMinutes());
    }

    public Optional<Voucher> findByCode(String code) {
        String infoKey = getInfoKey(code);
        Object raw = redisTemplate.opsForValue().get(infoKey);
        if (raw == null) {
            return Optional.empty();
        }

        try {
            Voucher voucher;
            if (raw instanceof Voucher) {
                voucher = (Voucher) raw;
            } else {
                voucher = objectMapper.convertValue(raw, Voucher.class);
            }

            // Đồng bộ stock hiện tại từ Redis
            Integer remainingStock = getRemainingStock(code);
            if (remainingStock != null && voucher.getTotalUsageLimit() > 0 && remainingStock >= 0) {
                voucher.setUsedCount(voucher.getTotalUsageLimit() - remainingStock);
            }

            return Optional.of(voucher);
        } catch (Exception e) {
            log.error("Lỗi parse voucher {} từ Redis: {}", code, e.getMessage());
            return Optional.empty();
        }
    }

    public List<Voucher> findAll() {
        Set<Object> members = redisTemplate.opsForSet().members(KEY_ALL_CODES);
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }

        List<Voucher> list = new ArrayList<>();
        List<String> expiredOrDeletedCodes = new ArrayList<>();

        for (Object item : members) {
            String code = item.toString();
            Optional<Voucher> voucherOpt = findByCode(code);
            if (voucherOpt.isPresent()) {
                list.add(voucherOpt.get());
            } else {
                // Key trong Redis đã hết hạn hoặc bị xóa -> dọn dẹp khỏi Set
                expiredOrDeletedCodes.add(code);
            }
        }

        if (!expiredOrDeletedCodes.isEmpty()) {
            redisTemplate.opsForSet().remove(KEY_ALL_CODES, expiredOrDeletedCodes.toArray());
        }

        // Sắp xếp voucher mới tạo lên đầu
        list.sort((a, b) -> {
            if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            return 0;
        });

        return list;
    }

    public void delete(String code) {
        String normalizedCode = code.trim().toUpperCase();
        redisTemplate.delete(getInfoKey(normalizedCode));
        redisTemplate.delete(getStockKey(normalizedCode));
        redisTemplate.delete(getUserUsageKey(normalizedCode));
        redisTemplate.opsForSet().remove(KEY_ALL_CODES, normalizedCode);
        log.info("Đã xóa voucher {} khỏi Redis", normalizedCode);
    }


    public Integer getRemainingStock(String code) {
        String stockKey = getStockKey(code);
        Object val = redisTemplate.opsForValue().get(stockKey);
        if (val == null) {
            return null;
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean decrementStock(String code) {
        String stockKey = getStockKey(code);
        Long result = redisTemplate.execute(decrementScript, Collections.singletonList(stockKey));
        return result != null && result == 1L;
    }

    public void incrementStock(String code) {
        String stockKey = getStockKey(code);
        redisTemplate.opsForValue().increment(stockKey);
    }

    public int getUserUsageCount(String code, String userId) {
        if (userId == null || userId.isBlank()) {
            return 0;
        }
        String key = getUserUsageKey(code);
        Object count = redisTemplate.opsForHash().get(key, userId);
        if (count == null) {
            return 0;
        }
        try {
            return Integer.parseInt(count.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public void recordUserUsage(String code, String userId, Duration ttl) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String key = getUserUsageKey(code);
        redisTemplate.opsForHash().increment(key, userId, 1);
        if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.expire(key, ttl);
        }
    }

    public void revertUserUsage(String code, String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String key = getUserUsageKey(code);
        Long current = redisTemplate.opsForHash().increment(key, userId, -1);
        if (current != null && current <= 0) {
            redisTemplate.opsForHash().delete(key, userId);
        }
    }
}
