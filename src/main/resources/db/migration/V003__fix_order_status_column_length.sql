-- Fix order_status column length to accommodate DOCUMENT_PENDING (16 chars)
ALTER TABLE transport_order MODIFY COLUMN order_status VARCHAR(20);
