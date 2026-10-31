-- Flyway V6: Create user_saved_addresses and user_fcm_tokens tables

CREATE TABLE IF NOT EXISTS user_saved_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    address_type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    address VARCHAR(500) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_saved_addresses_user_id ON user_saved_addresses(user_id);

CREATE TABLE IF NOT EXISTS user_fcm_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    fcm_token VARCHAR(512) NOT NULL UNIQUE,
    device_type VARCHAR(20) DEFAULT 'ANDROID',
    device_model VARCHAR(100),
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fcm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON user_fcm_tokens(user_id);

