ALTER TABLE transactions ADD COLUMN IF NOT EXISTS driver_id BIGINT;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS payment_url VARCHAR(4096);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS request_id VARCHAR(255);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS fulfillment_done BOOLEAN NOT NULL DEFAULT TRUE;
-- Existing successful payments must not be paid out a second time on deployment.
ALTER TABLE transactions ALTER COLUMN fulfillment_done SET DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_transactions_fulfillment ON transactions(status, fulfillment_done, transaction_type);
CREATE INDEX IF NOT EXISTS idx_transactions_business ON transactions(booking_id, transaction_type);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS fulfillment_next_attempt_at TIMESTAMP;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS fulfillment_attempts INTEGER NOT NULL DEFAULT 0;
