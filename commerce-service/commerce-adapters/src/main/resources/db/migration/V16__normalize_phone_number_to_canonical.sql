-- #4605 전화번호 canonical 포맷(하이픈 미포함) 통일 마이그레이션
-- orders/shipping_address 의 phone_number 계열 컬럼을 canonical 로 정규화한다.

UPDATE orders
SET delivery_recipient_phone_number = REPLACE(delivery_recipient_phone_number, '-', '')
WHERE delivery_recipient_phone_number LIKE '%-%';

UPDATE orders
SET pickup_recipient_phone_number = REPLACE(pickup_recipient_phone_number, '-', '')
WHERE pickup_recipient_phone_number LIKE '%-%';

UPDATE shipping_address_read_models
SET recipient_phone_number = REPLACE(recipient_phone_number, '-', '')
WHERE recipient_phone_number LIKE '%-%';
