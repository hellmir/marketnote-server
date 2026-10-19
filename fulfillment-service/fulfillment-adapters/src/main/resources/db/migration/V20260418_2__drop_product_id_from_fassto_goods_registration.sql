-- #4085: 풀필먼트 goods_registration productKey 기반 전환 (2단계) — product_id 컬럼 제거
-- Note: 운영 배포 전 기존 row들의 product_key backfill이 완료되었다고 가정.
--       신규 등록은 product_key만 사용하며, product_id는 더 이상 보관하지 않는다.
ALTER TABLE fassto_goods_registration
    ALTER COLUMN product_key SET NOT NULL;

ALTER TABLE fassto_goods_registration
    DROP COLUMN product_id;
