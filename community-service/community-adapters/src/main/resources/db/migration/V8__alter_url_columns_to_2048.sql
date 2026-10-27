-- URL 컬럼 길이 상한을 VARCHAR(2048) 표준으로 정렬
-- post.product_image_url 은 이미 VARCHAR(2048)이므로 제외
ALTER TABLE review ALTER COLUMN product_image_url TYPE VARCHAR(2048);
ALTER TABLE image_read_models ALTER COLUMN image_url TYPE VARCHAR(2048);
