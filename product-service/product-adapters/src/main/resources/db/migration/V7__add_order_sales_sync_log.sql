-- ============================================================
-- order_sales_sync_log (#4039)
-- OrderPurchaseConfirmedEvent 소비 시 판매량 증가 중복 수신 방어용 멱등 로그.
-- (order_id) 유니크 제약으로 동일 주문의 재처리를 차단한다.
-- ============================================================
CREATE TABLE order_sales_sync_log (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_id BIGINT NOT NULL,
    CONSTRAINT uk_order_sales_sync_log_order_id UNIQUE (order_id)
);
