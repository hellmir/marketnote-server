-- flyway:executeInTransaction=false

-- CREATE INDEX CONCURRENTLY 실패 시 복구 절차:
--   1) INVALID 인덱스 식별
--      SELECT c.relname FROM pg_class c
--        JOIN pg_index i ON i.indexrelid = c.oid
--        WHERE NOT i.indisvalid AND c.relname LIKE 'idx_price_policy_%' OR c.relname LIKE 'idx_product_category_%';
--   2) 수동 DROP 후 재실행 (IF NOT EXISTS는 INVALID도 존재로 간주하여 skip하므로 DROP 선행 필수)
--      DROP INDEX CONCURRENTLY <invalid_index_name>;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_policy_product_id_status
    ON price_policy (product_id, status);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_policy_status_popularity_id
    ON price_policy (status, popularity, id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_policy_status_discount_price_id
    ON price_policy (status, discount_price, id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_policy_status_accumulated_point_id
    ON price_policy (status, accumulated_point, id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_policy_status_accumulation_rate_id
    ON price_policy (status, accumulation_rate, id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_category_category_id
    ON product_category (category_id);
