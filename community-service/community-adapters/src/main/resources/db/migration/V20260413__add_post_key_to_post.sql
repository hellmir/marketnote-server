-- gen_random_uuid() 함수를 위한 확장 보장 (PostgreSQL 13+ 코어 내장이지만 명시적으로 안전 확보)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. post_key 컬럼 추가 (nullable로 먼저 생성)
ALTER TABLE post
    ADD COLUMN post_key UUID;

-- 2. 기존 데이터 백필: 신규 게시글은 UUID v7 (시간 순서 보장) 으로 발급되지만,
--    기존 레코드는 단순 식별자로만 사용되므로 UUID v4 (gen_random_uuid) 로 채워도 무방하다.
UPDATE post
SET post_key = gen_random_uuid();

-- 3. NOT NULL 제약 추가
ALTER TABLE post
    ALTER COLUMN post_key SET NOT NULL;

-- 4. UNIQUE 인덱스 생성
CREATE UNIQUE INDEX IF NOT EXISTS uk_post_post_key ON post (post_key);
