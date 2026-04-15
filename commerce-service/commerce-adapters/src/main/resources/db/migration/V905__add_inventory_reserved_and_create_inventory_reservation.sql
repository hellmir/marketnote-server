ALTER TABLE inventory ADD COLUMN reserved INT NOT NULL DEFAULT 0;

CREATE TABLE inventory_reservation (
    id               BIGSERIAL       PRIMARY KEY,
    order_id         BIGINT          NOT NULL,
    price_policy_id  BIGINT          NOT NULL,
    quantity         INT             NOT NULL,
    reserved_at      TIMESTAMP       NOT NULL,
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    modified_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_inventory_reservation_order_policy UNIQUE (order_id, price_policy_id)
);

CREATE INDEX idx_inventory_reservation_price_policy_id ON inventory_reservation (price_policy_id);
CREATE INDEX idx_inventory_reservation_reserved_at ON inventory_reservation (reserved_at);
