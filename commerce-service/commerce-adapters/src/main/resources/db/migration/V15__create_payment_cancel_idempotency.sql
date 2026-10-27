-- 결제 취소 API 멱등성 레코드 테이블
-- Idempotency-Key 헤더 기반 중복 방지 (TTL 24h, 일 배치 삭제)
CREATE TABLE IF NOT EXISTS payment_cancel_idempotency
(
    id              BIGSERIAL    PRIMARY KEY,
    idempotency_key VARCHAR(64)  NOT NULL,
    payment_id      BIGINT       NOT NULL,
    buyer_id        BIGINT       NOT NULL,
    status          VARCHAR(31)  NOT NULL,
    result          JSONB,
    failed_reason   TEXT,
    created_at      TIMESTAMP    NOT NULL,
    modified_at     TIMESTAMP    NOT NULL,
    CONSTRAINT uk_payment_cancel_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_payment_cancel_idempotency_created_at
    ON payment_cancel_idempotency (created_at);

COMMENT ON TABLE payment_cancel_idempotency IS '결제 취소 API 멱등성 레코드 (24시간 TTL)';
COMMENT ON COLUMN payment_cancel_idempotency.idempotency_key IS 'Idempotency-Key 헤더 값 (UUID v4)';
COMMENT ON COLUMN payment_cancel_idempotency.status IS 'IN_PROGRESS | COMPLETED | FAILED';
COMMENT ON COLUMN payment_cancel_idempotency.result IS '완료 응답 캐시 (JSON)';
COMMENT ON COLUMN payment_cancel_idempotency.failed_reason IS '실패 사유';
