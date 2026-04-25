-- #1946: CANCEL_REQUESTED → CANCELLED 마이그레이션
-- orders 테이블
UPDATE orders SET order_status = 'CANCELLED' WHERE order_status = 'CANCEL_REQUESTED';

-- order_product 테이블
UPDATE order_product SET order_status = 'CANCELLED' WHERE order_status = 'CANCEL_REQUESTED';

-- order_status_history 테이블
UPDATE order_status_history SET order_status = 'CANCELLED' WHERE order_status = 'CANCEL_REQUESTED';
