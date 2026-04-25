-- #1947: PREPARED → PREPARING 마이그레이션
-- orders 테이블
UPDATE orders SET order_status = 'PREPARING' WHERE order_status = 'PREPARED';

-- order_product 테이블
UPDATE order_product SET order_status = 'PREPARING' WHERE order_status = 'PREPARED';

-- order_status_history 테이블
UPDATE order_status_history SET order_status = 'PREPARING' WHERE order_status = 'PREPARED';
