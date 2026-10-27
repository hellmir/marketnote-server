-- URL 컬럼 길이 상한을 VARCHAR(2048) 표준으로 정렬
ALTER TABLE cart_product ALTER COLUMN image_url TYPE VARCHAR(2048);
ALTER TABLE image_read_models ALTER COLUMN image_url TYPE VARCHAR(2048);
ALTER TABLE image_read_model_resized_files ALTER COLUMN storage_url TYPE VARCHAR(2048);
ALTER TABLE product_catalog_read_model ALTER COLUMN representative_image_url TYPE VARCHAR(2048);
