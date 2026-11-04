package com.trung.bookingservice.service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="TEST_POSTGRES_URL", matches=".+")
class FinancialMigrationIntegrationTest {
    @Test void adoptsExistingTablesWithoutLosingPendingWork() throws Exception {
        verifyMigration(true);
    }
    @Test void createsTablesOnFreshDatabase() throws Exception {
        verifyMigration(false);
    }
    private void verifyMigration(boolean existing) throws Exception {
        String url = System.getenv("TEST_POSTGRES_URL");
        String schema = "migration_" + UUID.randomUUID().toString().replace("-", "");
        var admin = new JdbcTemplate(new DriverManagerDataSource(url, "codex_test", ""));
        admin.execute("create schema " + schema);
        var ds = new DriverManagerDataSource(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema, "codex_test", "");
        var jdbc = new JdbcTemplate(ds);
        try {
            jdbc.execute("create table bookings(id bigint primary key, status varchar(30) not null)");
            jdbc.update("insert into bookings values (1,'IN_PROGRESS')");
            if (existing) {
                for (String file : new String[]{"V20__financial_outbox.sql", "V21__order_idempotency.sql"}) {
                    try (var input = new ClassPathResource("db/migration/" + file).getInputStream()) {
                        jdbc.execute(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                    }
                }
                jdbc.update("insert into financial_outbox(order_id,attempts,last_error) values (99,3,'pending')");
                jdbc.update("insert into order_idempotency(customer_id,request_key,fingerprint,state,response,order_id) values (1,'retry-key','hash','SUCCESS','saved',99)");
            }
            var flyway = Flyway.configure().dataSource(ds).schemas(schema).baselineVersion("19").load();
            flyway.baseline();
            assertEquals(4, flyway.migrate().migrationsExecuted);
            flyway.validate();
            assertEquals(0, jdbc.queryForObject("select discount_amount from bookings where id=1", java.math.BigDecimal.class).signum());
            assertNull(jdbc.queryForObject("select original_amount from bookings where id=1", java.math.BigDecimal.class));
            assertEquals(false, jdbc.queryForObject("select prepaid from bookings where id=1", Boolean.class));
            jdbc.update("insert into bookings(id,status,prepaid) values (2,'AWAITING_PAYMENT',true)");
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertEquals(existing ? 1 : 0, jdbc.queryForObject("select count(*) from financial_outbox", Integer.class));
            assertEquals(existing ? 1 : 0, jdbc.queryForObject("select count(*) from order_idempotency", Integer.class));
            if (existing) {
                assertEquals(3, jdbc.queryForObject("select attempts from financial_outbox where order_id=99", Integer.class));
                assertEquals("saved", jdbc.queryForObject("select response from order_idempotency where customer_id=1", String.class));
            }
        } finally {
            admin.execute("drop schema " + schema + " cascade");
        }
    }
}