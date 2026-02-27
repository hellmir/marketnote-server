-- Issue #880: Race Condition 방지를 위한 partial unique index 추가
-- 기존 order_key unique constraint를 제거하고, READY/EXECUTING 상태에서만 유니크 제약 적용
-- 실행 방법: psql -d <database> -f V880__add_psp_payment_event_partial_unique_index.sql

-- 1. 기존 unique constraint 제거
ALTER TABLE psp_payment_event DROP CONSTRAINT IF EXISTS psp_payment_event_order_key_key;

-- 2. Partial unique index 생성: READY 또는 EXECUTING 상태인 활성 이벤트만 order_key 유니크 적용
CREATE UNIQUE INDEX IF NOT EXISTS idx_psp_payment_event_order_key_active
    ON psp_payment_event (order_key)
    WHERE po_status IN ('READY', 'EXECUTING');
