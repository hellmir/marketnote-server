-- ============================================================
-- product_catalog_read_models
-- ProductCatalogReadModel: 상품 목록 조회 전용 비정규화 테이블
-- (price_policy + product + review/stock/image 집계)
-- ============================================================
CREATE TABLE product_catalog_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    price_policy_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    primary_category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    price BIGINT NOT NULL,
    discount_price BIGINT NOT NULL,
    accumulated_point BIGINT NOT NULL,
    accumulated_point_rate NUMERIC(4, 1) NOT NULL,
    order_num BIGINT NOT NULL DEFAULT 0,
    popularity BIGINT NOT NULL DEFAULT 0,
    representative_image_url VARCHAR(511),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    review_count INTEGER NOT NULL DEFAULT 0,
    review_average_rating REAL NOT NULL DEFAULT 0,
    CONSTRAINT uk_product_catalog_read_model_price_policy_id UNIQUE (price_policy_id)
);

CREATE INDEX idx_product_catalog_read_model_product_id
    ON product_catalog_read_models (product_id);

CREATE INDEX idx_product_catalog_read_model_category
    ON product_catalog_read_models (primary_category_id, status);

CREATE INDEX idx_product_catalog_read_model_sort_order_num
    ON product_catalog_read_models (status, order_num, id);

CREATE INDEX idx_product_catalog_read_model_sort_popularity
    ON product_catalog_read_models (status, popularity, id);

CREATE INDEX idx_product_catalog_read_model_sort_discount_price
    ON product_catalog_read_models (status, discount_price, id);

CREATE INDEX idx_product_catalog_read_model_sort_accumulated_point
    ON product_catalog_read_models (status, accumulated_point, id);

CREATE INDEX idx_product_catalog_read_model_sort_accumulated_point_rate
    ON product_catalog_read_models (status, accumulated_point_rate, id);
