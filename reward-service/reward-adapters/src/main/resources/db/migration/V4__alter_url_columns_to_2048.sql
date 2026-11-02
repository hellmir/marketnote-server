-- URL 컬럼 길이 상한을 VARCHAR(2048) 표준으로 정렬
ALTER TABLE gifticon_brand ALTER COLUMN brand_image_url TYPE VARCHAR(2048);
ALTER TABLE gifticon_category ALTER COLUMN icon_url TYPE VARCHAR(2048);
ALTER TABLE gifticon_goods ALTER COLUMN brand_image_url TYPE VARCHAR(2048);
ALTER TABLE gifticon_goods ALTER COLUMN image_url TYPE VARCHAR(2048);
ALTER TABLE gifticon_order ALTER COLUMN product_image_url TYPE VARCHAR(2048);
ALTER TABLE gifticon_order ALTER COLUMN coupon_image_url TYPE VARCHAR(2048);
