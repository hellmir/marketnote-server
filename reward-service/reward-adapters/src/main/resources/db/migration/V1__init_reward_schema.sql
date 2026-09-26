-- ============================================================
-- user_point (PK = user_id, 외부 주입)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_point (
    user_id BIGINT PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_key VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,
    add_expected_amount BIGINT NOT NULL,
    expire_expected_amount BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_point_user_key ON user_point (user_key);

-- ============================================================
-- user_point_history (BaseEntity 미상속, @CreationTimestamp created_at만)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_point_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    change_type VARCHAR(15) NOT NULL,
    amount BIGINT NOT NULL,
    reflected_yn BOOLEAN NOT NULL,
    source_type VARCHAR(15) NOT NULL,
    source_id BIGINT NOT NULL,
    reason VARCHAR(255),
    accumulated_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_user_point_history_source UNIQUE (user_id, source_type, source_id, reason)
);

-- ============================================================
-- user_attendance (AuditingEntityListener + @CreatedDate만)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_attendance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    year VARCHAR(7),
    month VARCHAR(15),
    total_reward_quantity BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

-- ============================================================
-- user_attendance_history (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_attendance_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_attendance_id BIGINT NOT NULL,
    attendance_policy_id SMALLINT,
    reward_type VARCHAR(31) NOT NULL,
    reward_quantity BIGINT NOT NULL,
    continuous_period SMALLINT NOT NULL,
    reward_yn BOOLEAN NOT NULL,
    attended_date DATE NOT NULL,
    attended_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_user_attendance_history_attendance_date UNIQUE (user_attendance_id, attended_date)
);

-- ============================================================
-- attendance_policy (PK Short, BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS attendance_policy (
    id SMALLSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    continuous_period SMALLINT NOT NULL,
    reward_type VARCHAR(31) NOT NULL,
    reward_quantity BIGINT NOT NULL,
    attendence_date DATE,
    status VARCHAR(255) NOT NULL,
    order_num SMALLINT
);

-- ============================================================
-- gifticon_brand (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS gifticon_brand (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    brand_code VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255) NOT NULL,
    brand_image_url VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_gifticon_brand_brand_code ON gifticon_brand (brand_code);

-- ============================================================
-- gifticon_category (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS gifticon_category (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    category_code VARCHAR(255) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    icon_url VARCHAR(255),
    exposed BOOLEAN NOT NULL,
    order_num INTEGER
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_gifticon_category_category_code ON gifticon_category (category_code);

-- ============================================================
-- gifticon_category_mapping (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS gifticon_category_mapping (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    giftishow_category_seq VARCHAR(255) NOT NULL,
    gifticon_category_id BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_gifticon_category_mapping_giftishow_category_seq
    ON gifticon_category_mapping (giftishow_category_seq);

-- ============================================================
-- gifticon_goods (BaseEntity, goods_status는 Converter 기반 VARCHAR)
-- ============================================================
CREATE TABLE IF NOT EXISTS gifticon_goods (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    goods_code VARCHAR(255) NOT NULL,
    goods_name VARCHAR(255) NOT NULL,
    brand_code VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255) NOT NULL,
    brand_image_url VARCHAR(255),
    category_code VARCHAR(255),
    real_price BIGINT NOT NULL,
    sale_price BIGINT NOT NULL,
    cash_price BIGINT NOT NULL,
    image_url VARCHAR(255),
    description TEXT,
    valid_days INTEGER,
    goods_status VARCHAR(255) NOT NULL,
    exposed BOOLEAN NOT NULL,
    popular BOOLEAN NOT NULL,
    order_num INTEGER
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_gifticon_goods_goods_code ON gifticon_goods (goods_code);

-- ============================================================
-- gifticon_order (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS gifticon_order (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    goods_code VARCHAR(255) NOT NULL,
    goods_name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    product_image_url VARCHAR(255),
    tr_id VARCHAR(255) NOT NULL,
    order_no VARCHAR(255),
    cash_price BIGINT NOT NULL,
    order_status VARCHAR(255) NOT NULL,
    coupon_image_url VARCHAR(255),
    pin_no VARCHAR(255),
    valid_end_date DATE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_gifticon_order_tr_id ON gifticon_order (tr_id);

-- ============================================================
-- offerwall_mapper (AuditingEntityListener + @CreatedDate만)
-- ============================================================
CREATE TABLE IF NOT EXISTS offerwall_mapper (
    id BIGSERIAL PRIMARY KEY,
    offerwall_type VARCHAR(15) NOT NULL,
    reward_key VARCHAR(256) NOT NULL,
    user_key VARCHAR(128) NOT NULL,
    user_device_type VARCHAR(15) NOT NULL,
    campaign_key VARCHAR(50) NOT NULL,
    campaign_type INTEGER,
    campaign_name VARCHAR(256),
    quantity BIGINT NOT NULL,
    signed_value VARCHAR(256) NOT NULL,
    app_key INTEGER,
    app_name VARCHAR(50),
    adid VARCHAR(128),
    idfa VARCHAR(128),
    success_yn BOOLEAN NOT NULL,
    failure_count SMALLINT DEFAULT 0 NOT NULL,
    attended_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_offerwall_type_reward_key_success_failure_desc
    ON offerwall_mapper (offerwall_type, reward_key, success_yn, failure_count DESC);

-- ============================================================
-- reward_vendor_communication_history (AuditingEntityListener + @CreatedDate만)
-- ============================================================
CREATE TABLE IF NOT EXISTS reward_vendor_communication_history (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(31) NOT NULL,
    target_id VARCHAR(63),
    vendor_name VARCHAR(31) NOT NULL,
    communication_type VARCHAR(15) NOT NULL,
    sender VARCHAR(15) NOT NULL,
    exception VARCHAR(63),
    payload TEXT,
    payload_json JSONB,
    created_at TIMESTAMP(6) NOT NULL
);

-- ============================================================
-- reward_service_communication_failure_history (AuditingEntityListener + @CreatedDate만)
-- ============================================================
CREATE TABLE IF NOT EXISTS reward_service_communication_failure_history (
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
