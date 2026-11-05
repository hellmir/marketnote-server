-- [#215][#1196][#4106] OrderAmount.paidAmount Money VO 전환: NOT NULL + DEFAULT 0 제약 적용
-- 기존 결제 완료 주문 중 paidAmount가 NULL로 남은 레코드를 0으로 채우고,
-- 신규 주문은 초기값 0으로 저장되도록 DEFAULT 0 적용 후 NOT NULL 제약을 추가한다.

UPDATE orders SET paid_amount = 0 WHERE paid_amount IS NULL;

ALTER TABLE orders ALTER COLUMN paid_amount SET DEFAULT 0;
ALTER TABLE orders ALTER COLUMN paid_amount SET NOT NULL;
