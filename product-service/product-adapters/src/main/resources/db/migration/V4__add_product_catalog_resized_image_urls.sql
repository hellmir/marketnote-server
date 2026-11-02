-- ============================================================
-- product_catalog_read_models 컬럼 보강
-- - resized_image_urls TEXT: resized 이미지 URL JSON 배열 문자열
-- - discount_rate NUMERIC(4,1): price_policy 원본 할인율 보존 (재계산 금지)
-- ============================================================
ALTER TABLE product_catalog_read_models
    ADD COLUMN resized_image_urls TEXT;

ALTER TABLE product_catalog_read_models
    ADD COLUMN discount_rate NUMERIC(4, 1) NOT NULL DEFAULT 0;
