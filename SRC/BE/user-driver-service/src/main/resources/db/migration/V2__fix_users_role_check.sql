-- Flyway V2: Update users_role_check constraint to include RESTAURANT
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (role IN ('CUSTOMER', 'DRIVER', 'ADMIN', 'RESTAURANT'));
