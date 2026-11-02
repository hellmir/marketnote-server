-- URL 컬럼 길이 상한을 VARCHAR(2048) 표준으로 정렬
ALTER TABLE order_product ALTER COLUMN image_url TYPE VARCHAR(2048);
