-- 주문 테이블: 구매자별 최근 주문 조회 최적화
CREATE INDEX IF NOT EXISTS idx_orders_buyer_id_created_at
    ON orders (buyer_id, created_at);

-- 결제 테이블: 주문 ID 기반 결제 조회 최적화
CREATE INDEX IF NOT EXISTS idx_payment_order_id
    ON payment (order_id);

-- PSP 결제 이벤트 테이블: 주문 키 기반 조회 최적화
CREATE INDEX IF NOT EXISTS idx_psp_payment_event_order_key
    ON psp_payment_event (order_key);

-- 결제 배분 테이블: 기간별 정산 대상 조회 최적화
CREATE INDEX IF NOT EXISTS idx_payment_allocation_created_at_settlement_id
    ON payment_allocation (created_at, settlement_id);
