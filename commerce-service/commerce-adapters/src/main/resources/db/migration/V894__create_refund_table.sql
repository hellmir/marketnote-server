CREATE TABLE refund (
    id              BIGSERIAL       PRIMARY KEY,
    payment_id      BIGINT          NOT NULL,
    order_id        BIGINT          NOT NULL,
    refund_type     VARCHAR(20)     NOT NULL,
    refund_amount   BIGINT          NOT NULL,
    cancel_reason   VARCHAR(500),
    processed_by    VARCHAR(100),
    pg_refund_key   VARCHAR(200),
    pg_raw_response TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    modified_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refund_order_id ON refund (order_id);
CREATE INDEX idx_refund_payment_id ON refund (payment_id);
