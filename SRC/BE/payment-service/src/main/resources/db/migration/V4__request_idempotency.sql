CREATE TABLE customer_order_admission (
 customer_id BIGINT PRIMARY KEY, kind VARCHAR(8) NOT NULL,
 request_key VARCHAR(128) NOT NULL, fingerprint VARCHAR(64) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE payment_idempotency (
 scope VARCHAR(100) NOT NULL, request_key VARCHAR(128) NOT NULL,
 fingerprint VARCHAR(64) NOT NULL, provider_reference VARCHAR(255), response TEXT,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(scope, request_key)
);
