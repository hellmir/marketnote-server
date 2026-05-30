CREATE TABLE return_tracker
(
    id                 BIGSERIAL PRIMARY KEY,
    order_id           BIGINT      NOT NULL UNIQUE,
    return_slip_number VARCHAR(100),
    inspection_status  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    refund_status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    inspected_at       TIMESTAMP,
    refunded_at        TIMESTAMP,
    created_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    modified_at        TIMESTAMP
);

CREATE INDEX idx_return_tracker_inspection_status ON return_tracker (inspection_status);
