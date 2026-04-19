CREATE TABLE IF NOT EXISTS settlement
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    seller_id
    BIGINT
    NOT
    NULL,
    year
    INTEGER
    NOT
    NULL,
    month
    INTEGER
    NOT
    NULL,
    total_allocated_amount
    BIGINT
    NOT
    NULL
    CHECK
(
    total_allocated_amount >
    0
),
    pg_fee_amount BIGINT NOT NULL DEFAULT 0,
    platform_fee_amount BIGINT NOT NULL DEFAULT 0,
    seller_payout_amount BIGINT NOT NULL CHECK
(
    seller_payout_amount >
    0
),
    status VARCHAR
(
    15
) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP
    );

-- 유니크 제약: 판매자/년/월 조합 (동일 판매자 동일 월 중복 정산 방지)
CREATE UNIQUE INDEX IF NOT EXISTS idx_settlement_seller_year_month
    ON settlement (seller_id, year, month);

-- 조회용 인덱스
CREATE INDEX IF NOT EXISTS idx_settlement_year_month
    ON settlement (year, month);

CREATE INDEX IF NOT EXISTS idx_settlement_status
    ON settlement (status);

-- payment_allocation 테이블에 settlement FK 추가
ALTER TABLE payment_allocation
    ADD CONSTRAINT fk_payment_allocation_settlement
        FOREIGN KEY (settlement_id) REFERENCES settlement (id);
