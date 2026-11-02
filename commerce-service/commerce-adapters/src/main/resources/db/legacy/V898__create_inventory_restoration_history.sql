CREATE TABLE inventory_restoration_history
(
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT,
    price_policy_id BIGINT    NOT NULL,
    order_id        BIGINT,
    stock           INT       NOT NULL,
    reason          VARCHAR(511),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    modified_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_inventory_restoration_order_policy
    ON inventory_restoration_history (order_id, price_policy_id) WHERE order_id IS NOT NULL;
