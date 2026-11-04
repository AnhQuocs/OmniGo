ALTER TABLE wallets DROP CONSTRAINT IF EXISTS wallets_user_type_check;
ALTER TABLE wallets ADD CONSTRAINT wallets_user_type_check
    CHECK (user_type IN ('DRIVER', 'CUSTOMER', 'ADMIN', 'RESTAURANT'));
