CREATE TABLE financial_outbox (
    order_id BIGINT PRIMARY KEY,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error VARCHAR(500)
);
