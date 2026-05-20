-- Remove name fields from users table
ALTER TABLE users DROP COLUMN first_name;
ALTER TABLE users DROP COLUMN last_name;

-- Add name fields to client table
ALTER TABLE client ADD COLUMN first_name VARCHAR(150);
ALTER TABLE client ADD COLUMN last_name VARCHAR(150);

-- Add name fields to driver table
ALTER TABLE driver ADD COLUMN first_name VARCHAR(150);
ALTER TABLE driver ADD COLUMN last_name VARCHAR(150);
