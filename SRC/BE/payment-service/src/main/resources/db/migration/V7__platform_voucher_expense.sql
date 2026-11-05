-- Platform-funded voucher cost; no recipient wallet is debited.
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check
    CHECK (transaction_type IN (
        'DEPOSIT', 'TRIP_PAYMENT', 'TRIP_INCOME', 'COMMISSION_FEE',
        'WITHDRAWAL', 'FOOD_PAYMENT', 'FOOD_DELIVERY_INCOME',
        'FOOD_RESTAURANT_INCOME', 'REFUND', 'PLATFORM_VOUCHER_EXPENSE'
    ));
