CREATE TABLE IF NOT EXISTS order_idempotency (
 customer_id BIGINT NOT NULL, request_key VARCHAR(128) NOT NULL,
 fingerprint VARCHAR(64) NOT NULL, state VARCHAR(16) NOT NULL,
 response TEXT, order_id BIGINT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (customer_id, request_key)
);
