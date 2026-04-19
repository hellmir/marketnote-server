-- 1. writer_masked_name 컬럼 추가 (nullable로 먼저 생성)
ALTER TABLE post
    ADD COLUMN writer_masked_name VARCHAR(15);

-- 2. 기존 데이터 마이그레이션: 현재 writer_name에 마스킹 값이 저장되어 있으므로 writer_masked_name에 복사
UPDATE post
SET writer_masked_name = writer_name
WHERE writer_masked_name IS NULL;

-- 3. NOT NULL 제약 추가
ALTER TABLE post
    ALTER COLUMN writer_masked_name SET NOT NULL;
