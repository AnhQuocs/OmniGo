-- Flyway V5: Drop Hibernate-generated check constraints on food_orders (status & cancel_reason_code)
ALTER TABLE food_orders DROP CONSTRAINT IF EXISTS food_orders_status_check;
ALTER TABLE food_orders DROP CONSTRAINT IF EXISTS food_orders_cancel_reason_code_check;

-- Drop any other check constraint on status column dynamically
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'food_orders'::regclass
          AND contype = 'c'
          AND conname LIKE '%status%'
    ) LOOP
        EXECUTE 'ALTER TABLE food_orders DROP CONSTRAINT IF EXISTS ' || quote_ident(r.conname);
    END LOOP;
END $$;
