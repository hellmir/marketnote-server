CREATE INDEX IF NOT EXISTS idx_order_product_order_id
    ON order_product (order_id);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id
    ON order_status_history (order_id);

CREATE INDEX IF NOT EXISTS idx_inventory_reservation_reserved_at
    ON inventory_reservation (reserved_at);
