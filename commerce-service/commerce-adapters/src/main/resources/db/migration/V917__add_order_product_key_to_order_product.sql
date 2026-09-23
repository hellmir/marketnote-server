CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE order_product ADD COLUMN order_product_key UUID;

UPDATE order_product SET order_product_key = gen_random_uuid() WHERE order_product_key IS NULL;

ALTER TABLE order_product ALTER COLUMN order_product_key SET NOT NULL;

ALTER TABLE order_product ADD CONSTRAINT uk_order_product_order_product_key UNIQUE (order_product_key);
