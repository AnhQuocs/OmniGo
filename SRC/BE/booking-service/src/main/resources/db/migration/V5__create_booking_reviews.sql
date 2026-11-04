CREATE TABLE IF NOT EXISTS booking_reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    driver_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(255),
    customer_avatar_url VARCHAR(2048),
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_booking_reviews_driver_id ON booking_reviews(driver_id);
CREATE INDEX IF NOT EXISTS idx_booking_reviews_booking_id ON booking_reviews(booking_id);

