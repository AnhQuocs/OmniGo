-- Flyway V1: Initial schema for booking-service
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    driver_id BIGINT,
    start_longitude DOUBLE PRECISION NOT NULL,
    start_latitude DOUBLE PRECISION NOT NULL,
    end_longitude DOUBLE PRECISION NOT NULL,
    end_latitude DOUBLE PRECISION NOT NULL,
    distance_in_km DOUBLE PRECISION,
    status VARCHAR(30) NOT NULL,
    price DOUBLE PRECISION,
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    completed_at TIMESTAMP
);
