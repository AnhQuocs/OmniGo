-- Older Hibernate-created schemas do not allow FOOD_RESTAURANT_INCOME.
-- Replacing the check preserves the ledger and keeps invalid types rejected.
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check
    CHECK (transaction_type IN (
        'DEPOSIT', 'TRIP_PAYMENT', 'TRIP_INCOME', 'COMMISSION_FEE',
        'WITHDRAWAL', 'FOOD_PAYMENT', 'FOOD_DELIVERY_INCOME',
        'FOOD_RESTAURANT_INCOME', 'REFUND'
    ));
