-- FAQ 게시글 카테고리 코드 재구성: 7개 → 5개
-- ORDER_PAYMENT, DELIVERY     → ORDER_DELIVERY
-- CANCEL_REFUND, RETURN_EXCHANGE → CANCEL_RETURN
-- POINT, EVENT_COUPON         → BENEFIT_REWARD
-- LOGIN_USER_INFO             → ACCOUNT
-- PRODUCT                     → 신규 (이관 대상 없음)
UPDATE post
SET category = 'ORDER_DELIVERY'
WHERE board = 'FAQ'
  AND category IN ('ORDER_PAYMENT', 'DELIVERY');

UPDATE post
SET category = 'CANCEL_RETURN'
WHERE board = 'FAQ'
  AND category IN ('CANCEL_REFUND', 'RETURN_EXCHANGE');

UPDATE post
SET category = 'BENEFIT_REWARD'
WHERE board = 'FAQ'
  AND category IN ('POINT', 'EVENT_COUPON');

UPDATE post
SET category = 'ACCOUNT'
WHERE board = 'FAQ'
  AND category = 'LOGIN_USER_INFO';

-- 매핑 누락 검증: 신규 5개 외 카테고리가 잔존하면 마이그레이션 실패
DO $$
DECLARE
    leftover_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO leftover_count
    FROM post
    WHERE board = 'FAQ'
      AND category NOT IN ('ORDER_DELIVERY', 'CANCEL_RETURN', 'BENEFIT_REWARD', 'PRODUCT', 'ACCOUNT');

    IF leftover_count > 0 THEN
        RAISE EXCEPTION 'V9 FAQ 카테고리 마이그레이션 누락: % 건', leftover_count;
    END IF;
END $$;
