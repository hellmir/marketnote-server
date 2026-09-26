-- #1944: REFUND 상태 → RETURN 상태 마이그레이션
-- orders 테이블
UPDATE orders
SET order_status = 'RETURN_REQUESTED'
WHERE order_status = 'REFUND_REQUESTED';
UPDATE orders
SET order_status = 'RETURN_IN_PROGRESS'
WHERE order_status = 'REFUND_RECALLING';
UPDATE orders
SET order_status = 'RETURN_IN_PROGRESS'
WHERE order_status = 'REFUND_SHIPPING';
UPDATE orders
SET order_status = 'RETURNED'
WHERE order_status = 'REFUNDED';
UPDATE orders
SET order_status = 'PARTIALLY_RETURNED'
WHERE order_status = 'PARTIALLY_REFUNDED';

-- order_product 테이블
UPDATE order_product
SET order_status = 'RETURN_REQUESTED'
WHERE order_status = 'REFUND_REQUESTED';
UPDATE order_product
SET order_status = 'RETURN_IN_PROGRESS'
WHERE order_status = 'REFUND_RECALLING';
UPDATE order_product
SET order_status = 'RETURN_IN_PROGRESS'
WHERE order_status = 'REFUND_SHIPPING';
UPDATE order_product
SET order_status = 'RETURNED'
WHERE order_status = 'REFUNDED';
UPDATE order_product
SET order_status = 'PARTIALLY_RETURNED'
WHERE order_status = 'PARTIALLY_REFUNDED';

-- order_status_history 테이블
UPDATE order_status_history
SET order_status = 'RETURN_REQUESTED'
WHERE order_status = 'REFUND_REQUESTED';
UPDATE order_status_history
SET order_status = 'RETURN_IN_PROGRESS'
WHERE order_status = 'REFUND_RECALLING';
UPDATE order_status_history
SET order_status = 'RETURN_IN_PROGRESS'
WHERE order_status = 'REFUND_SHIPPING';
UPDATE order_status_history
SET order_status = 'RETURNED'
WHERE order_status = 'REFUNDED';
UPDATE order_status_history
SET order_status = 'PARTIALLY_RETURNED'
WHERE order_status = 'PARTIALLY_REFUNDED';
