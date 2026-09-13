-- Flyway V6: Create food_order_reviews table and add review_count to restaurants
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS review_count INT DEFAULT 0;

CREATE TABLE IF NOT EXISTS food_order_reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    restaurant_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100),
    driver_id BIGINT,
    
    -- Đánh giá quán ăn
    restaurant_rating INT NOT NULL CHECK (restaurant_rating BETWEEN 1 AND 5),
    restaurant_comment TEXT,
    restaurant_images TEXT,
    
    -- Đánh giá tài xế
    driver_rating INT CHECK (driver_rating BETWEEN 1 AND 5),
    driver_comment TEXT,
    
    -- Phản hồi của chủ quán
    merchant_reply TEXT,
    merchant_replied_at TIMESTAMP,
    
    -- Ràng buộc 48h chỉnh sửa
    editable_until TIMESTAMP NOT NULL,
    is_edited BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_review_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES food_orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reviews_restaurant_id ON food_order_reviews (restaurant_id);
CREATE INDEX IF NOT EXISTS idx_reviews_order_id ON food_order_reviews (order_id);
CREATE INDEX IF NOT EXISTS idx_reviews_customer_id ON food_order_reviews (customer_id);
