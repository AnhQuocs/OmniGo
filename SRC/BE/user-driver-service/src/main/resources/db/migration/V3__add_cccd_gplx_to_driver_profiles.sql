-- Flyway V3: Add CCCD and GPLX columns to driver_profiles
ALTER TABLE driver_profiles ADD COLUMN IF NOT EXISTS cccd_number VARCHAR(20);
ALTER TABLE driver_profiles ADD COLUMN IF NOT EXISTS cccd_front_image TEXT;
ALTER TABLE driver_profiles ADD COLUMN IF NOT EXISTS cccd_back_image TEXT;
ALTER TABLE driver_profiles ADD COLUMN IF NOT EXISTS gplx_number VARCHAR(30);
ALTER TABLE driver_profiles ADD COLUMN IF NOT EXISTS gplx_front_image TEXT;
ALTER TABLE driver_profiles ADD COLUMN IF NOT EXISTS gplx_back_image TEXT;
