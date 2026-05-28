-- V2: Add user profile fields (first name, last name, phone, structured address)
ALTER TABLE "user"
    ADD COLUMN IF NOT EXISTS first_name      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS last_name       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS phone           VARCHAR(20),
    ADD COLUMN IF NOT EXISTS address_street  VARCHAR(100),
    ADD COLUMN IF NOT EXISTS address_city    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS address_zip     VARCHAR(20),
    ADD COLUMN IF NOT EXISTS address_country VARCHAR(50);
