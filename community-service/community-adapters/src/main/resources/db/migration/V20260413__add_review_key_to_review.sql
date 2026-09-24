-- 1. review_key 컬럼 추가 (nullable로 먼저 생성)
ALTER TABLE review
    ADD COLUMN review_key UUID;

-- 2. 기존 데이터 백필: PostgreSQL gen_random_uuid()로 임의 UUID 채움
UPDATE review
SET review_key = gen_random_uuid()
WHERE review_key IS NULL;

-- 3. NOT NULL 제약 추가
ALTER TABLE review
    ALTER COLUMN review_key SET NOT NULL;

-- 4. UNIQUE 제약 추가
ALTER TABLE review
    ADD CONSTRAINT uq_review_review_key UNIQUE (review_key);
