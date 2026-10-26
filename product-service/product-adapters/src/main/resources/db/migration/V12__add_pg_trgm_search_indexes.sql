-- flyway:executeInTransaction=false

-- 상품 이름/브랜드명 LIKE '%keyword%' 선두 와일드카드 검색을 위한 pg_trgm GIN 인덱스
-- PostgreSQL 9.1+는 planner가 자동으로 LIKE 쿼리를 pg_trgm 인덱스로 가속

-- CREATE INDEX CONCURRENTLY 실패 시 복구 절차:
--   1) INVALID 인덱스 식별
--      SELECT c.relname FROM pg_class c
--        JOIN pg_index i ON i.indexrelid = c.oid
--        WHERE NOT i.indisvalid AND c.relname LIKE 'idx_product_%_trgm';
--   2) 수동 DROP 후 재실행 (IF NOT EXISTS는 INVALID도 존재로 간주하여 skip하므로 DROP 선행 필수)
--      DROP INDEX CONCURRENTLY <invalid_index_name>;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_name_trgm
    ON product USING GIN (name gin_trgm_ops);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_brand_name_trgm
    ON product USING GIN (brand_name gin_trgm_ops);
