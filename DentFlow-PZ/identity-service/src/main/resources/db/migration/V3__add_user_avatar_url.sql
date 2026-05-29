-- Dodanie pola avatar_url do tabeli "user"
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(1000);
