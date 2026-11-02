-- 결제 배분 테이블 (판매자별 결제 금액 배분)
CREATE TABLE IF NOT EXISTS payment_allocation
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    order_id
    BIGINT
    NOT
    NULL
    REFERENCES
    orders
(
    id
),
    seller_id BIGINT NOT NULL,
    allocated_amount BIGINT NOT NULL CHECK
(
    allocated_amount >
    0
),
    settlement_id BIGINT,
    transaction_type VARCHAR
(
    31
) NOT NULL,
    target_type VARCHAR
(
    15
) NOT NULL,
    idempotency_key VARCHAR
(
    255
) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_payment_allocation_order_id ON payment_allocation(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_allocation_seller_id ON payment_allocation(seller_id);
CREATE INDEX IF NOT EXISTS idx_payment_allocation_settlement_id ON payment_allocation(settlement_id);
CREATE INDEX IF NOT EXISTS idx_payment_allocation_seller_unsettled
    ON payment_allocation(seller_id) WHERE settlement_id IS NULL;
