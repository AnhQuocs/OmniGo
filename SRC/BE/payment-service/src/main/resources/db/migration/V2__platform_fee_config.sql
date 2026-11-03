CREATE TABLE platform_fee_config (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    percentage NUMERIC(5,2) NOT NULL CHECK (percentage BETWEEN 0 AND 100)
);
INSERT INTO platform_fee_config (id, percentage) VALUES (1, 20.00);
