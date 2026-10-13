-- Flyway V5: Insert default admin user if not exists
INSERT INTO users (phone_number, email, password, full_name, role, is_deleted, is_locked, created_at, updated_at)
VALUES (
    '0923456789',
    'admin@ridehailingsystem.online',
    '$2a$10$ydFKXenFW5MgLydqFst8FevOlrsZvEPqlzJYlxk3uxuvQ6N9jLN8O',
    'Administrator',
    'ADMIN',
    FALSE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (phone_number) DO NOTHING;
