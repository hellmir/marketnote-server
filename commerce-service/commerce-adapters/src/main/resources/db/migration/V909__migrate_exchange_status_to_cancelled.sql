-- #1945: EXCHANGE 상태 → CANCELLED 마이그레이션 (교환 기능 삭제)
-- orders 테이블
UPDATE orders SET order_status = 'CANCELLED' WHERE order_status IN ('EXCHANGE_REQUESTED', 'EXCHANGE_SHIPPING', 'EXCHANGE_DELIVERED', 'EXCHANGE_COMPLETED');

-- order_product 테이블
UPDATE order_product SET order_status = 'CANCELLED' WHERE order_status IN ('EXCHANGE_REQUESTED', 'EXCHANGE_SHIPPING', 'EXCHANGE_DELIVERED', 'EXCHANGE_COMPLETED');

-- order_status_history 테이블
UPDATE order_status_history SET order_status = 'CANCELLED' WHERE order_status IN ('EXCHANGE_REQUESTED', 'EXCHANGE_SHIPPING', 'EXCHANGE_DELIVERED', 'EXCHANGE_COMPLETED');
