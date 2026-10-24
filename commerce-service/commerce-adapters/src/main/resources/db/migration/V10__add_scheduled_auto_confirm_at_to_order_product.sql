-- [#4307] order_product.scheduled_auto_confirm_at 컬럼 추가
-- 배송 완료 시점에 자동 확정 예정 일시를 저장 (deliveredAt + autoConfirmDays)
-- 자동 확정 스케줄러가 이 컬럼을 기준으로 배치 조회한다.

ALTER TABLE order_product
    ADD COLUMN scheduled_auto_confirm_at TIMESTAMP(6) NULL;

CREATE INDEX IF NOT EXISTS idx_order_product_scheduled_auto_confirm_at
    ON order_product (scheduled_auto_confirm_at);
