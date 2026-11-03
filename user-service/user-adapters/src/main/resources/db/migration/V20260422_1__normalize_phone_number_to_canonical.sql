-- #4605 전화번호 canonical 포맷(하이픈 미포함) 통일 마이그레이션
-- 하이픈 포함 저장값을 canonical 로 정규화하여 `PhoneNumber.equals()` 비교 정합성과
-- UNIQUE 제약 기반 중복 탐지 정확도를 복원한다.
--
-- [배포 전 사전 점검 권장 쿼리 — 스테이징/운영에서 수동 실행]
--   SELECT REPLACE(phone_number, '-', '') AS normalized, COUNT(*), ARRAY_AGG(id)
--   FROM users WHERE phone_number IS NOT NULL
--   GROUP BY 1 HAVING COUNT(*) > 1;

-- 1) users.phone_number UNIQUE 제약: 정규화 시 기존 canonical 값과 충돌 발생 가능 → 사전 감지
DO $$
DECLARE
    conflict_count INT;
BEGIN
    SELECT COUNT(*) INTO conflict_count
    FROM (
        SELECT REPLACE(phone_number, '-', '') AS normalized
        FROM users
        WHERE phone_number IS NOT NULL
        GROUP BY REPLACE(phone_number, '-', '')
        HAVING COUNT(*) > 1
    ) dup;

    IF conflict_count > 0 THEN
        RAISE EXCEPTION 'users.phone_number UNIQUE 충돌 예상 (중복 그룹 % 건). 운영팀 수동 정리 후 재실행 필요.', conflict_count;
    END IF;
END $$;

UPDATE users
SET phone_number = REPLACE(phone_number, '-', '')
WHERE phone_number LIKE '%-%';

-- 2) shipping_addresses.recipient_phone_number (UNIQUE 아님)
UPDATE shipping_addresses
SET recipient_phone_number = REPLACE(recipient_phone_number, '-', '')
WHERE recipient_phone_number LIKE '%-%';

-- 3) 검증: canonical 패턴 (01[016789] + 7~8자리) 미일치 잔존 값 검출
DO $$
DECLARE
    invalid_users INT;
    invalid_shipping INT;
BEGIN
    SELECT COUNT(*) INTO invalid_users
    FROM users
    WHERE phone_number IS NOT NULL
      AND phone_number !~ '^01[016789][0-9]{7,8}$';

    IF invalid_users > 0 THEN
        RAISE EXCEPTION 'users.phone_number canonical 패턴 미일치 % 건 잔존. 운영팀 수동 정리 필요.', invalid_users;
    END IF;

    SELECT COUNT(*) INTO invalid_shipping
    FROM shipping_addresses
    WHERE recipient_phone_number IS NOT NULL
      AND recipient_phone_number !~ '^01[016789][0-9]{7,8}$';

    IF invalid_shipping > 0 THEN
        RAISE EXCEPTION 'shipping_addresses.recipient_phone_number canonical 패턴 미일치 % 건 잔존. 운영팀 수동 정리 필요.', invalid_shipping;
    END IF;
END $$;
