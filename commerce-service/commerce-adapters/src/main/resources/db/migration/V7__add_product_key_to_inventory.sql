ALTER TABLE inventory
    ADD COLUMN product_key UUID;

CREATE INDEX IF NOT EXISTS idx_inventory_product_key ON inventory (product_key);
