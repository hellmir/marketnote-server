ALTER TABLE orders ADD COLUMN delivery_request_type VARCHAR(31);
ALTER TABLE orders ADD COLUMN pickup_delivery_request_type VARCHAR(31);

UPDATE orders SET delivery_request_type = 'CUSTOM' WHERE delivery_request_message IS NOT NULL AND delivery_request_message != '';
UPDATE orders SET delivery_request_type = 'NONE' WHERE delivery_request_type IS NULL;

ALTER TABLE orders ALTER COLUMN delivery_request_message TYPE VARCHAR(60);
