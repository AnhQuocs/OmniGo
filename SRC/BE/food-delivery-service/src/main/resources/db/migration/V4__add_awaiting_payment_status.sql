-- Flyway V4: Support AWAITING_PAYMENT in food_orders
-- Ensure status column accepts up to 30 characters
ALTER TABLE food_orders ALTER COLUMN status TYPE VARCHAR(30);

-- Drop old check constraint if exists to ensure AWAITING_PAYMENT is valid
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_food_orders_status'
    ) THEN
        ALTER TABLE food_orders DROP CONSTRAINT chk_food_orders_status;
    END IF;
END $$;
