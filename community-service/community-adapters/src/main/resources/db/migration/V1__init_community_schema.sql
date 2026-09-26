-- ============================================================
-- post (BaseOrderedGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS post (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    user_id BIGINT NOT NULL,
    post_key UUID NOT NULL,
    parent_id BIGINT,
    board VARCHAR(31) NOT NULL,
    target_group_type VARCHAR(31),
    target_group_id BIGINT,
    target_type VARCHAR(31),
    target_id BIGINT,
    category VARCHAR(63) NOT NULL,
    product_image_url VARCHAR(2048),
    writer_name VARCHAR(15) NOT NULL,
    masked_writer_name VARCHAR(15) NOT NULL,
    title VARCHAR(255),
    content VARCHAR(8192) NOT NULL,
    is_private BOOLEAN NOT NULL,
    is_photo BOOLEAN NOT NULL,
    CONSTRAINT uk_post_post_key UNIQUE (post_key)
);

-- ============================================================
-- review (BaseOrderedGeneralEntity, likeCount는 @Formula)
-- ============================================================
CREATE TABLE IF NOT EXISTS review (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    review_key UUID NOT NULL,
    reviewer_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    price_policy_id BIGINT NOT NULL,
    product_image_url VARCHAR(2047),
    selected_options VARCHAR(127),
    quantity INTEGER NOT NULL,
    reviewer_name VARCHAR(15) NOT NULL,
    masked_reviewer_name VARCHAR(15) NOT NULL,
    rating REAL NOT NULL,
    content VARCHAR(8191) NOT NULL,
    photo_yn BOOLEAN NOT NULL,
    edited_yn BOOLEAN DEFAULT FALSE NOT NULL,
    unit_amount BIGINT,
    CONSTRAINT uk_review_review_key UNIQUE (review_key)
);

-- ============================================================
-- likes (BaseEntity + EmbeddedId)
-- ============================================================
CREATE TABLE IF NOT EXISTS likes (
    target_type VARCHAR(15) NOT NULL,
    target_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(15) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    PRIMARY KEY (target_type, target_id, user_id)
);

-- ============================================================
-- report (BaseEntity 미상속, @CreatedDate only, EmbeddedId)
-- ============================================================
CREATE TABLE IF NOT EXISTS report (
    target_type VARCHAR(15) NOT NULL,
    target_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(2047) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (target_type, target_id, reporter_id)
);

-- ============================================================
-- profanity_words (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS profanity_words (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    word VARCHAR(50) NOT NULL,
    CONSTRAINT uk_profanity_words_word UNIQUE (word)
);

-- ============================================================
-- product_review_aggregate (BaseEntity + 자체 @Id product_id)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_review_aggregate (
    product_id BIGINT PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    total_count INT DEFAULT 1 NOT NULL,
    five_point_count INT DEFAULT 0 NOT NULL,
    four_point_count INT DEFAULT 0 NOT NULL,
    three_point_count INT DEFAULT 0 NOT NULL,
    two_point_count INT DEFAULT 0 NOT NULL,
    one_point_count INT DEFAULT 0 NOT NULL,
    total_rating REAL,
    average_rating REAL NOT NULL
);

-- ============================================================
-- review_version_history (BaseEntity + 자체 id BIGSERIAL)
-- ============================================================
CREATE TABLE IF NOT EXISTS review_version_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    review_id BIGINT NOT NULL,
    rating REAL NOT NULL,
    content VARCHAR(8192),
    photo_yn BOOLEAN NOT NULL
);

-- ============================================================
-- image_read_models (BaseGeneralEntity)
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
-- product_read_models (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    price_policy_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    price BIGINT,
    discount_price BIGINT,
    accumulated_point BIGINT,
    CONSTRAINT uk_product_read_model_price_policy_id UNIQUE (price_policy_id)
);

-- ============================================================
-- community_service_communication_failure_history (BaseEntity 미상속, @CreatedDate only)
-- ============================================================
CREATE TABLE IF NOT EXISTS community_service_communication_failure_history (
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
