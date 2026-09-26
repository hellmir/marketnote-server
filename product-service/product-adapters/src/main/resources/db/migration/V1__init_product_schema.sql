-- pgcrypto: UUID 생성 함수 보장 (기존 V20260413 백필 스크립트와 동일 전제)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- product
-- ============================================================
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    product_key UUID NOT NULL,
    seller_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    detail VARCHAR(1023),
    stock INT DEFAULT 0 NOT NULL,
    sales INT DEFAULT 0 NOT NULL,
    view_count BIGINT DEFAULT 0 NOT NULL,
    popularity BIGINT DEFAULT 0 NOT NULL,
    find_all_options_yn BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT uk_product_product_key UNIQUE (product_key)
);

-- ============================================================
-- product_tag
-- ============================================================
CREATE TABLE IF NOT EXISTS product_tag (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    product_id BIGINT NOT NULL,
    name VARCHAR(31) NOT NULL,
    CONSTRAINT fk_product_tag_product FOREIGN KEY (product_id) REFERENCES product (id)
);

-- ============================================================
-- category
-- ============================================================
CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    parent_category_id BIGINT,
    name VARCHAR(127) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_category_parent ON category (parent_category_id);

-- ============================================================
-- product_category (복합 PK, @IdClass)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_category (
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    PRIMARY KEY (product_id, category_id)
);

-- ============================================================
-- product_option_category
-- ============================================================
CREATE TABLE IF NOT EXISTS product_option_category (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_product_option_category_product FOREIGN KEY (product_id) REFERENCES product (id)
);

-- ============================================================
-- product_option
-- ============================================================
CREATE TABLE IF NOT EXISTS product_option (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    product_option_category_id BIGINT NOT NULL,
    content VARCHAR(511) NOT NULL,
    CONSTRAINT fk_product_option_product_option_category
        FOREIGN KEY (product_option_category_id) REFERENCES product_option_category (id)
);

-- ============================================================
-- price_policy
-- ============================================================
CREATE TABLE IF NOT EXISTS price_policy (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    product_id BIGINT NOT NULL,
    price BIGINT NOT NULL,
    discount_price BIGINT NOT NULL,
    discount_rate NUMERIC(4, 1) NOT NULL,
    accumulated_point BIGINT NOT NULL,
    accumulation_rate NUMERIC(4, 1) NOT NULL,
    popularity BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT fk_price_policy_product FOREIGN KEY (product_id) REFERENCES product (id)
);

-- ============================================================
-- product_option_price_policy (복합 PK, @EmbeddedId + @MapsId)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_option_price_policy (
    product_option_id BIGINT NOT NULL,
    price_policy_id BIGINT NOT NULL,
    PRIMARY KEY (product_option_id, price_policy_id),
    CONSTRAINT fk_pop_option FOREIGN KEY (product_option_id) REFERENCES product_option (id),
    CONSTRAINT fk_pop_price_policy FOREIGN KEY (price_policy_id) REFERENCES price_policy (id)
);

-- ============================================================
-- cart_product (복합 PK, @EmbeddedId + @MapsId)
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_product (
    user_id BIGINT NOT NULL,
    price_policy_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    sharer_key UUID,
    image_url VARCHAR(511),
    quantity SMALLINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, price_policy_id),
    CONSTRAINT fk_cart_product_price_policy FOREIGN KEY (price_policy_id) REFERENCES price_policy (id)
);

-- ============================================================
-- shipping_policy
-- ============================================================
CREATE TABLE IF NOT EXISTS shipping_policy (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    seller_id BIGINT NOT NULL,
    delivery_company VARCHAR(50) NOT NULL,
    shipping_fee BIGINT NOT NULL,
    free_shipping_threshold BIGINT NOT NULL,
    jeju_surcharge BIGINT,
    island_surcharge BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_shipping_policy_seller_id ON shipping_policy (seller_id);

-- ============================================================
-- image_read_models
-- ============================================================
CREATE TABLE IF NOT EXISTS image_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    image_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(31) NOT NULL,
    file_sort VARCHAR(63) NOT NULL,
    image_url VARCHAR(511) NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT uk_image_read_model_image_id UNIQUE (image_id)
);

CREATE INDEX IF NOT EXISTS idx_image_read_model_target_sort
    ON image_read_models (target_id, file_sort, status);

-- ============================================================
-- inventory_read_models
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    price_policy_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    stock_quantity INTEGER NOT NULL,
    CONSTRAINT uk_inventory_read_model_price_policy_id UNIQUE (price_policy_id)
);

-- ============================================================
-- review_aggregate_read_models
-- ============================================================
CREATE TABLE IF NOT EXISTS review_aggregate_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    product_id BIGINT NOT NULL,
    total_count INTEGER NOT NULL,
    average_rating REAL NOT NULL,
    CONSTRAINT uk_review_aggregate_read_model_product_id UNIQUE (product_id)
);

-- ============================================================
-- fulfillment_goods_read_models
-- ============================================================
CREATE TABLE IF NOT EXISTS fulfillment_goods_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    customer_goods_code VARCHAR(255) NOT NULL,
    goods_code VARCHAR(255),
    goods_name VARCHAR(255),
    goods_type VARCHAR(255),
    goods_type_name VARCHAR(255),
    invoice_goods_name_enabled VARCHAR(255),
    goods_option_code1 VARCHAR(255),
    goods_option_code2 VARCHAR(255),
    customer_code VARCHAR(255),
    customer_name VARCHAR(255),
    supplier_code VARCHAR(255),
    supplier_name VARCHAR(255),
    category_code VARCHAR(255),
    category_name VARCHAR(255),
    season_code VARCHAR(255),
    gender_code VARCHAR(255),
    unit_price VARCHAR(255),
    supply_price VARCHAR(255),
    sale_price VARCHAR(255),
    handling_temperature VARCHAR(255),
    picking_facility VARCHAR(255),
    gift_division VARCHAR(255),
    gift_division_name VARCHAR(255),
    goods_width VARCHAR(255),
    goods_length VARCHAR(255),
    goods_height VARCHAR(255),
    manufacture_year VARCHAR(255),
    goods_bulk VARCHAR(255),
    goods_weight VARCHAR(255),
    goods_side_sum VARCHAR(255),
    goods_volume VARCHAR(255),
    goods_barcode VARCHAR(255),
    box_width VARCHAR(255),
    box_length VARCHAR(255),
    box_height VARCHAR(255),
    box_bulk VARCHAR(255),
    box_weight VARCHAR(255),
    inner_box_barcode VARCHAR(255),
    inner_box_length VARCHAR(255),
    inner_box_height VARCHAR(255),
    inner_box_bulk VARCHAR(255),
    inner_box_width VARCHAR(255),
    inner_box_weight VARCHAR(255),
    inner_box_side_sum VARCHAR(255),
    box_inner_count VARCHAR(255),
    inner_box_inner_count VARCHAR(255),
    pallet_inner_count VARCHAR(255),
    origin VARCHAR(255),
    expiration_date_management_enabled VARCHAR(255),
    shelf_life_days VARCHAR(255),
    outbound_available_days VARCHAR(255),
    inbound_available_days VARCHAR(255),
    outbound_box_type VARCHAR(255),
    cushioning_enabled VARCHAR(255),
    loading_direction VARCHAR(255),
    first_inbound_date VARCHAR(255),
    enabled VARCHAR(255),
    fee_applied VARCHAR(255),
    sale_unit_quantity VARCHAR(255),
    one_day_delivery_enabled VARCHAR(255),
    safety_stock VARCHAR(255),
    CONSTRAINT uk_fulfillment_goods_read_model_customer_goods_code UNIQUE (customer_goods_code)
);

-- ============================================================
-- fulfillment_goods_element_read_models
-- ============================================================
CREATE TABLE IF NOT EXISTS fulfillment_goods_element_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    fulfillment_goods_id BIGINT NOT NULL,
    goods_code VARCHAR(255),
    customer_goods_code VARCHAR(255),
    goods_barcode VARCHAR(255),
    goods_name VARCHAR(255),
    goods_type VARCHAR(255),
    goods_type_name VARCHAR(255),
    quantity INTEGER,
    CONSTRAINT fk_fulfillment_goods_element_goods
        FOREIGN KEY (fulfillment_goods_id) REFERENCES fulfillment_goods_read_models (id)
);

-- ============================================================
-- product_service_communication_failure_history
-- (BaseEntity 미상속 — 독자적 감사 컬럼)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_service_communication_failure_history (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(31) NOT NULL,
    target_id VARCHAR(63),
    communication_type VARCHAR(15) NOT NULL,
    sender VARCHAR(15) NOT NULL,
    exception VARCHAR(63),
    payload TEXT,
    payload_json JSONB,
    created_at TIMESTAMP(6) NOT NULL
);

-- ============================================================
-- common: outbox_event
-- ============================================================
CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    partition_key VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    source VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL,
    max_retries INTEGER NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    published_at TIMESTAMP(6),
    failed_at TIMESTAMP(6),
    last_error_message VARCHAR(1000),
    discard_reason VARCHAR(500),
    discarded_at TIMESTAMP(6),
    CONSTRAINT uk_outbox_event_id UNIQUE (event_id)
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_event (status, created_at);

-- ============================================================
-- common: saga_instance
-- ============================================================
CREATE TABLE IF NOT EXISTS saga_instance (
    id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(100) NOT NULL,
    saga_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_step_index INTEGER NOT NULL,
    payload TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    CONSTRAINT uk_saga_instance_saga_id UNIQUE (saga_id)
);

CREATE INDEX IF NOT EXISTS idx_saga_instance_type_status ON saga_instance (saga_type, status);
CREATE INDEX IF NOT EXISTS idx_saga_instance_status_modified_at ON saga_instance (status, modified_at);

-- ============================================================
-- common: saga_step
-- ============================================================
CREATE TABLE IF NOT EXISTS saga_step (
    id BIGSERIAL PRIMARY KEY,
    saga_instance_id BIGINT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_index INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    request TEXT NOT NULL,
    response TEXT,
    compensation_request TEXT,
    compensation_response TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_saga_step_instance_id_index ON saga_step (saga_instance_id, step_index);

-- ============================================================
-- common: dlt_message_resolution
-- ============================================================
CREATE TABLE IF NOT EXISTS dlt_message_resolution (
    id BIGSERIAL PRIMARY KEY,
    original_topic VARCHAR(100) NOT NULL,
    dlt_topic VARCHAR(100) NOT NULL,
    partition_number INTEGER NOT NULL,
    offset_number BIGINT NOT NULL,
    resolution VARCHAR(20) NOT NULL,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP(6),
    reason VARCHAR(500),
    CONSTRAINT uk_dlt_resolution_topic_partition_offset UNIQUE (dlt_topic, partition_number, offset_number)
);

CREATE INDEX IF NOT EXISTS idx_dlt_resolution_original_topic ON dlt_message_resolution (original_topic);
