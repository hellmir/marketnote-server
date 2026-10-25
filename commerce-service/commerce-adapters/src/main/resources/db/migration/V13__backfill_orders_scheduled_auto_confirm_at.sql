-- [#4351] 기존 DELIVERED 주문(orders)에 scheduled_auto_confirm_at 백필
-- V11에서 채워진 order_product.scheduled_auto_confirm_at의 최대값을 주문 레벨에 반영한다.
-- 이미 확정/취소 등 후속 상태로 이동한 주문은 백필하지 않는다 (orders.order_status = 'DELIVERED'만 대상).
-- `IS NULL` 가드로 멱등성은 보장된다.

UPDATE orders o
SET scheduled_auto_confirm_at = sub.max_scheduled_auto_confirm_at
    FROM (
    SELECT op.order_id, MAX(op.scheduled_auto_confirm_at) AS max_scheduled_auto_confirm_at
    FROM order_product op
    WHERE op.scheduled_auto_confirm_at IS NOT NULL
    GROUP BY op.order_id
) AS sub
WHERE o.id = sub.order_id
  AND o.order_status = 'DELIVERED'
  AND o.scheduled_auto_confirm_at IS NULL;
