-- Add optional name fields to users for admin accounts
ALTER TABLE users ADD COLUMN first_name VARCHAR(150);
ALTER TABLE users ADD COLUMN last_name VARCHAR(150);
