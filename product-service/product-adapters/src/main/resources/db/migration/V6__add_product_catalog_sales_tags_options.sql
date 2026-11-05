-- ============================================================
-- product_catalog_read_models 필드 보강 (#4037)
-- - sales BIGINT: 판매량 (주문 확정 시 증가)
-- - product_tags TEXT: 상품 태그 JSON 배열 문자열 ([{"id","name","orderNum"}])
-- - selected_options TEXT: 선택 옵션 JSON 배열 문자열 ([{"id","content"}])
-- ============================================================
ALTER TABLE product_catalog_read_models
    ADD COLUMN sales BIGINT NOT NULL DEFAULT 0;

ALTER TABLE product_catalog_read_models
    ADD COLUMN product_tags TEXT;

ALTER TABLE product_catalog_read_models
    ADD COLUMN selected_options TEXT;

-- ============================================================
-- backfill: 운영 데이터에서 기존 ReadModel 행의 sales/tags/options 를 보강한다.
-- - sales: product.sales 값을 복사 (없으면 0). `cr.sales = 0` 가드는 '최초 backfill' 한정이며
--   이 마이그레이션을 재실행하지 않는 한(Flyway 가 중복 적용 안 함) 재계산은 발생하지 않는다.
--   단, sales=0 인 상품도 product.sales=0 로 동일하게 계산되므로 결과는 idempotent 하다.
-- - product_tags: 상관 스칼라 서브쿼리로 json_agg 직렬화 (순서: order_num ASC).
--   UI 노출 제한(2개)은 ReadModel → ProductItemResult 매핑 단계(MAX_DISPLAY_TAGS)에서 적용한다.
-- - selected_options: product_option_price_policy × product_option JOIN 후 json_agg 직렬화.
-- 모두 PostgreSQL 상관 서브쿼리만 사용 — LATERAL 불필요, H2 호환은 Flyway 운영 DB 한정.
-- ============================================================
UPDATE product_catalog_read_models cr
SET sales = COALESCE((
        SELECT p.sales
        FROM product p
        WHERE p.id = cr.product_id
    ), 0)
WHERE cr.sales = 0;

UPDATE product_catalog_read_models cr
SET product_tags = (
        SELECT CASE
                   WHEN COUNT(t.id) = 0 THEN NULL
                   ELSE json_agg(
                           json_build_object(
                                   'id', t.id,
                                   'name', t.name,
                                   'orderNum', t.order_num
                           )
                           ORDER BY t.order_num ASC
                   )::text
               END
        FROM product_tag t
        WHERE t.product_id = cr.product_id
          AND t.status = 'ACTIVE'
    )
WHERE cr.product_tags IS NULL;

UPDATE product_catalog_read_models cr
SET selected_options = (
        SELECT CASE
                   WHEN COUNT(po.id) = 0 THEN NULL
                   ELSE json_agg(
                           json_build_object(
                                   'id', po.id,
                                   'content', po.content
                           )
                           ORDER BY po.id ASC
                   )::text
               END
        FROM product_option_price_policy popp
        JOIN product_option po
            ON po.id = popp.product_option_id
        WHERE popp.price_policy_id = cr.price_policy_id
          AND po.status = 'ACTIVE'
    )
WHERE cr.selected_options IS NULL;
