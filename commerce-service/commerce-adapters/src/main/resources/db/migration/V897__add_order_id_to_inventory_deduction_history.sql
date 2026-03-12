-- 재고 차감 이력에 orderId 컬럼 추가 (멱등성 보장)
ALTER TABLE inventory_deduction_history ADD COLUMN order_id BIGINT;

-- 기존 데이터는 NULL 유지, 신규 데이터만 UNIQUE 제약 적용 (partial index)
CREATE UNIQUE INDEX uk_inventory_deduction_order_policy
    ON inventory_deduction_history (order_id, price_policy_id)
    WHERE order_id IS NOT NULL;
