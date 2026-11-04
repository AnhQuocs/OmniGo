package com.trung.paymentservice.service;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
@Service @RequiredArgsConstructor
public class PaymentLocks {
    private final JdbcTemplate jdbc;
    @Transactional(propagation = Propagation.MANDATORY)
    public void lock(String key) {
        jdbc.query("select pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> {}, key);
    }
}
