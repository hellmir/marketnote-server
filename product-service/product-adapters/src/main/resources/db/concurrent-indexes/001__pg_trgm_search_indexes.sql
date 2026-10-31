-- pg_trgm GIN 인덱스 — 상품명/브랜드명 LIKE '%keyword%' 선두 와일드카드 검색 가속
--
-- 이 스크립트는 Flyway가 아니라 Jenkins 전용 'Run Concurrent Indexes' 스테이지에서
-- psql을 통해 직접 실행된다. (Flyway schema history 락 커넥션과
-- CREATE INDEX CONCURRENTLY 가 자기 교착하는 이슈를 회피하기 위함)
--
-- 요구사항:
--   - 모든 문장은 idempotent (IF NOT EXISTS). 반복 실행 가능해야 한다.
--   - CREATE INDEX CONCURRENTLY 는 트랜잭션 내부에서 실행 금지. psql -f 기본 동작(auto-commit)에 의존.
--   - INVALID 상태의 인덱스가 남아 있으면 IF NOT EXISTS 가 재생성을 skip 하므로,
--     Jenkins 스테이지의 bash 래퍼(run-concurrent-indexes.sh)에서 선행 DROP 을 수행한다.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_name_trgm
    ON product USING GIN (name gin_trgm_ops);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_brand_name_trgm
    ON product USING GIN (brand_name gin_trgm_ops);
