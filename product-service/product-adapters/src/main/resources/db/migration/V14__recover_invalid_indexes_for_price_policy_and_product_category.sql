-- flyway:executeInTransaction=false

-- V10에서 CREATE INDEX CONCURRENTLY 실행 중 중단 시 PostgreSQL은 INVALID 상태 인덱스를 남긴다.
-- IF NOT EXISTS는 INVALID 인덱스도 "존재"로 간주하여 skip하므로 V10 재실행으로는 복구되지 않는다.
-- 이 마이그레이션은 대상 6개 인덱스 중 INVALID 상태만 선제 DROP(비-CONCURRENTLY, DO 블록 내에서 가능)한 후,
-- CREATE INDEX CONCURRENTLY IF NOT EXISTS로 재생성하여 자동 복구한다.
-- 참고: DO 블록 내부에서는 CREATE INDEX CONCURRENTLY 실행 불가 → DO 블록과 CREATE를 분리된 statement로 작성.
-- 관련 이슈: #4501

-- 1) INVALID 인덱스만 선제 DROP (카탈로그 조회 + 동적 SQL)
-- 주의: DO 블록 내부에서는 DROP INDEX CONCURRENTLY 실행 불가 → 비-CONCURRENTLY DROP 사용.
--       이 경우 대상 테이블에 ACCESS EXCLUSIVE 락을 획득하므로 DML이 락 대기 상태로 들어간다.
--       INVALID 인덱스 DROP은 통상 수 밀리초 수준이나, 트래픽 피크 시간대 배포는 지양 권장.
-- 네임스페이스 한정: current_schema()로 현재 스키마에 한정하여 동명 인덱스 충돌 방지.
DO $$
DECLARE
    invalid_index RECORD;
BEGIN
    FOR invalid_index IN
        SELECT n.nspname, c.relname
        FROM pg_index i
        JOIN pg_class c ON c.oid = i.indexrelid
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE NOT i.indisvalid
          AND n.nspname = current_schema()
          AND c.relname IN (
              'idx_price_policy_product_id_status',
              'idx_price_policy_status_popularity_id',
              'idx_price_policy_status_discount_price_id',
              'idx_price_policy_status_accumulated_point_id',
              'idx_price_policy_status_accumulation_rate_id',
              'idx_product_category_category_id'
          )
        ORDER BY c.relname
    LOOP
        RAISE NOTICE 'Dropping INVALID index: %.%', invalid_index.nspname, invalid_index.relname;
        EXECUTE 'DROP INDEX ' || quote_ident(invalid_index.nspname) || '.' || quote_ident(invalid_index.relname);
    END LOOP;
END $$;

-- 2) 누락/삭제된 인덱스 재생성 (V10과 동일 정의)
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
