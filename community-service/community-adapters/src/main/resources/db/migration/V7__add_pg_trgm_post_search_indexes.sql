-- flyway:executeInTransaction=false

-- 게시글 제목/본문 LOWER(col) LIKE '%keyword%' 선두 와일드카드 검색을 위한 pg_trgm GIN expression 인덱스
-- PostPersistenceAdapter는 '%' + searchKeyword.toLowerCase() + '%' 패턴 사용 → LOWER() expression 인덱스로 case-insensitive 가속

-- CREATE INDEX CONCURRENTLY 실패 시 복구 절차:
--   1) INVALID 인덱스 식별
--      SELECT c.relname FROM pg_class c
--        JOIN pg_index i ON i.indexrelid = c.oid
--        WHERE NOT i.indisvalid AND c.relname LIKE 'idx_post_%_trgm';
--   2) 수동 DROP 후 재실행
--      DROP INDEX CONCURRENTLY <invalid_index_name>;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_post_title_lower_trgm
    ON post USING GIN (LOWER(title) gin_trgm_ops);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_post_content_lower_trgm
    ON post USING GIN (LOWER(content) gin_trgm_ops);
