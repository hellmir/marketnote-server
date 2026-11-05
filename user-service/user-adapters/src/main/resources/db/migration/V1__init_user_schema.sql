-- ============================================================
-- role (BaseEntity 미상속, 마스터 데이터)
-- ============================================================
CREATE TABLE IF NOT EXISTS role (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_role_name UNIQUE (name)
);

-- ============================================================
-- terms (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS terms (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    content VARCHAR(511) NOT NULL,
    required_yn BOOLEAN
);

-- ============================================================
-- users (BaseOrderedGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_num BIGINT,
    user_key UUID NOT NULL,
    nickname VARCHAR(31),
    email VARCHAR(255),
    password VARCHAR(511),
    full_name VARCHAR(15),
    phone_number VARCHAR(15),
    reference_code VARCHAR(15),
    referred_user_code VARCHAR(15),
    role_id VARCHAR(255),
    signed_up_at TIMESTAMP(6) NOT NULL,
    last_logged_in_at TIMESTAMP(6) NOT NULL,
    withdrawn_yn BOOLEAN DEFAULT FALSE NOT NULL,
    withdrawn_at TIMESTAMP(6),
    penalty_count INTEGER NOT NULL,
    deactivated_until TIMESTAMP(6),
    CONSTRAINT uk_users_user_key UNIQUE (user_key),
    CONSTRAINT uk_users_nickname UNIQUE (nickname),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number),
    CONSTRAINT uk_users_reference_code UNIQUE (reference_code),
    CONSTRAINT uk_users_referred_user_code UNIQUE (referred_user_code),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES role (id)
);

-- ============================================================
-- user_oauth2_vendor (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_oauth2_vendor (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    auth_vendor VARCHAR(15) NOT NULL,
    oidc_id VARCHAR(255),
    CONSTRAINT fk_user_oauth2_vendor_users FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ============================================================
-- user_terms (BaseGeneralEntity, 복합 unique)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_terms (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    terms_id BIGINT NOT NULL,
    agreement_yn BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT ux_user_terms_user_terms UNIQUE (user_id, terms_id),
    CONSTRAINT fk_user_terms_users FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_terms_terms FOREIGN KEY (terms_id) REFERENCES terms (id)
);

-- ============================================================
-- login_history (BaseEntity, status 컬럼 없음)
-- ============================================================
CREATE TABLE IF NOT EXISTS login_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT,
    auth_vendor VARCHAR(255),
    ip_address VARCHAR(255),
    CONSTRAINT fk_login_history_users FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ============================================================
-- shipping_addresses (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS shipping_addresses (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    address_type VARCHAR(15) NOT NULL,
    address VARCHAR(255) NOT NULL,
    address_detail VARCHAR(255) NOT NULL,
    company_name VARCHAR(63),
    address_alias VARCHAR(255),
    recipient_name VARCHAR(31) NOT NULL,
    recipient_phone_number VARCHAR(15) NOT NULL,
    delivery_request_type VARCHAR(31) NOT NULL,
    delivery_request_message VARCHAR(60),
    is_default BOOLEAN DEFAULT FALSE NOT NULL,
    region_type VARCHAR(15) NOT NULL
);

-- ============================================================
-- remote_areas (BaseGeneralEntity, 엔티티 기준 재정의)
-- ============================================================
CREATE TABLE IF NOT EXISTS remote_areas (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    province VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    village VARCHAR(50) NOT NULL,
    subarea VARCHAR(50) NOT NULL,
    region_type VARCHAR(25) NOT NULL,
    CONSTRAINT uk_remote_areas_location UNIQUE (province, district, village, subarea)
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
-- user_service_communication_failure_history (BaseEntity 미상속, @CreatedDate only)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_service_communication_failure_history (
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
-- user_penalty_histories (BaseEntity, status 없음)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_penalty_histories (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    previous_count INTEGER NOT NULL,
    current_count INTEGER NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_by BIGINT NOT NULL,
    CONSTRAINT fk_user_penalty_histories_users FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ============================================================
-- user_status_histories (BaseEntity, status 없음)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_status_histories (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    status_action VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    deactivated_until TIMESTAMP(6),
    actor VARCHAR(20) NOT NULL,
    created_by BIGINT
);

CREATE INDEX IF NOT EXISTS idx_user_status_histories_user_id ON user_status_histories (user_id);

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
