-- Flyway V2: Alter image_url column in restaurants and menu_items to TEXT
ALTER TABLE restaurants ALTER COLUMN image_url TYPE TEXT;
ALTER TABLE menu_items ALTER COLUMN image_url TYPE TEXT;
