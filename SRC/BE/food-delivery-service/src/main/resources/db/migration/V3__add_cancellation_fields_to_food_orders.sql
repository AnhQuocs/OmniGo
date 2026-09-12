-- Flyway V3: Add cancellation tracking and driver retry fields to food_orders
ALTER TABLE food_orders ADD COLUMN IF NOT EXISTS cancelled_by VARCHAR(30);
ALTER TABLE food_orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255);
ALTER TABLE food_orders ADD COLUMN IF NOT EXISTS cancel_reason_code VARCHAR(50);
ALTER TABLE food_orders ADD COLUMN IF NOT EXISTS driver_retry_count INTEGER DEFAULT 0;
UPDATE food_orders SET driver_retry_count = 0 WHERE driver_retry_count IS NULL;
