-- [#4351] orders.scheduled_auto_confirm_at 컬럼 추가
-- Order가 DELIVERED 상태로 전이될 때 저장. PARTIALLY_* 부분 상태로 전이된 경우에는 설정되지 않는다.
-- 주문 조회 응답에서 클라이언트에게 노출되는 값이다.

ALTER TABLE orders
    ADD COLUMN scheduled_auto_confirm_at TIMESTAMP(6) NULL;
