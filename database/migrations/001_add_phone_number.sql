-- Approved migration only. Run once on the existing fashion_ai database.
-- Existing accounts retain NULL phone numbers; no existing data is updated.
ALTER TABLE fashion_ai.users
    ADD COLUMN phone_number VARCHAR(20) NULL,
    ADD CONSTRAINT uq_users_phone_number UNIQUE (phone_number);
