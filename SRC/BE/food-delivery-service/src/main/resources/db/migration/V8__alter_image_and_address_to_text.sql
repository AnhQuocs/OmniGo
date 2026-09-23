-- Flyway V8: Ensure image_url, address, and license_image_url are TEXT
ALTER TABLE restaurants ALTER COLUMN image_url TYPE TEXT;
ALTER TABLE restaurants ALTER COLUMN address TYPE TEXT;
ALTER TABLE menu_items ALTER COLUMN image_url TYPE TEXT;
