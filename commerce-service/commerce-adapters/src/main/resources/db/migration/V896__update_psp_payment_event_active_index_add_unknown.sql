-- UNKNOWN 상태 이벤트도 동일 orderKey 중복 방지
DROP INDEX IF EXISTS idx_psp_payment_event_order_key_active;
CREATE UNIQUE INDEX idx_psp_payment_event_order_key_active
    ON psp_payment_event (order_key)
    WHERE po_status IN ('READY', 'EXECUTING', 'UNKNOWN');
