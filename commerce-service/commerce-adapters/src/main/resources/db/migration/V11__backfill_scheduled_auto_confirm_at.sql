-- [#4307] 기존 DELIVERED 주문 상품의 scheduled_auto_confirm_at 백필
-- 배송 완료(delivered_at)가 설정된 상품에 대해 +7일로 자동 확정 예정 일시를 채운다.
-- 이미 확정된 주문(CONFIRMED 등)은 백필하지 않는다 (scheduler가 재조회하지 않도록).
--
-- 주의: 7일 상수는 `order.auto-confirm.scheduler.auto-confirm-days` 기본값과 동일하다.
-- 운영 환경에서 `ORDER_AUTO_CONFIRM_DAYS` 환경 변수로 다른 값을 사용 중이라면
-- 이 마이그레이션 적용 전 해당 값에 맞춰 `INTERVAL 'N days'`를 조정해야 한다.
-- `IS NULL` 가드로 멱등성은 보장된다.

UPDATE order_product
SET scheduled_auto_confirm_at = delivered_at + INTERVAL '7 days'
WHERE order_status = 'DELIVERED'
  AND delivered_at IS NOT NULL
  AND scheduled_auto_confirm_at IS NULL;
