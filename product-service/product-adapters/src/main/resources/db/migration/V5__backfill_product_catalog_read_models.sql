-- ============================================================
-- product_catalog_read_models 운영 데이터 backfill
-- ReadModel 조회 경로 전환(#3993) 을 위해 기존 상품/가격정책/이미지/재고/리뷰 집계 ReadModel 을 조합하여 삽입한다.
-- 대표 이미지 / resized 이미지 URL 은 상관 서브쿼리로 조회하여 price_policy × image 카티시안을 회피한다.
-- 이미 존재하는 price_policy_id 는 skip(NOT EXISTS) — 재실행 안전.
-- ============================================================
INSERT INTO product_catalog_read_models (
    status,
    created_at,
    modified_at,
    price_policy_id,
    product_id,
    seller_id,
    primary_category_id,
    name,
    brand_name,
    price,
    discount_price,
    discount_rate,
    accumulated_point,
    accumulated_point_rate,
    order_num,
    popularity,
    representative_image_url,
    resized_image_urls,
    stock_quantity,
    review_count,
    review_average_rating
)
SELECT
    'ACTIVE' AS status,
    pp.created_at,
    pp.modified_at,
    pp.id AS price_policy_id,
    p.id AS product_id,
    p.seller_id,
    (
        SELECT MIN(pc.category_id)
        FROM product_category pc
        WHERE pc.product_id = p.id
    ) AS primary_category_id,
    p.name,
    p.brand_name,
    pp.price,
    pp.discount_price,
    COALESCE(pp.discount_rate, 0) AS discount_rate,
    pp.accumulated_point,
    pp.accumulation_rate AS accumulated_point_rate,
    COALESCE(p.order_num, 0) AS order_num,
    COALESCE(pp.popularity, 0) AS popularity,
    (
        SELECT img.image_url
        FROM image_read_models img
        WHERE img.target_id = p.id
          AND img.file_sort = 'PRODUCT_CATALOG_IMAGE'
          AND img.sort_order = 0
          AND img.status = 'ACTIVE'
        ORDER BY img.id ASC
        LIMIT 1
    ) AS representative_image_url,
    (
        SELECT CASE
                   WHEN COUNT(f.id) = 0 THEN NULL
                   ELSE json_agg(f.storage_url ORDER BY f.sort_order)::text
               END
        FROM image_read_model_resized_files f
        WHERE f.image_read_model_id = (
            SELECT img2.id
            FROM image_read_models img2
            WHERE img2.target_id = p.id
              AND img2.file_sort = 'PRODUCT_CATALOG_IMAGE'
              AND img2.sort_order = 0
              AND img2.status = 'ACTIVE'
            ORDER BY img2.id ASC
            LIMIT 1
        )
          AND f.status = 'ACTIVE'
    ) AS resized_image_urls,
    COALESCE(inv.stock_quantity, 0) AS stock_quantity,
    COALESCE(rev.total_count, 0) AS review_count,
    COALESCE(rev.average_rating, 0) AS review_average_rating
FROM price_policy pp
JOIN product p
    ON pp.product_id = p.id
LEFT JOIN inventory_read_models inv
    ON inv.price_policy_id = pp.id
   AND inv.status = 'ACTIVE'
LEFT JOIN review_aggregate_read_models rev
    ON rev.product_id = p.id
   AND rev.status = 'ACTIVE'
WHERE p.status = 'ACTIVE'
  AND pp.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1
      FROM product_catalog_read_models cr
      WHERE cr.price_policy_id = pp.id
  );
