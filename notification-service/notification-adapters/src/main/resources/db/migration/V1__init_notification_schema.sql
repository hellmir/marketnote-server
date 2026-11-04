CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(500) NOT NULL,
    data TEXT,
    delivery_channel VARCHAR(30) NOT NULL,
    is_read BOOLEAN NOT NULL,
    landing_url VARCHAR(500),
    send_status VARCHAR(20) NOT NULL,
    scheduled_at TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id_status ON notification (user_id, status);

CREATE TABLE IF NOT EXISTS notification_template (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    template_code VARCHAR(100) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    notification_category VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body_template VARCHAR(500) NOT NULL,
    url_template VARCHAR(500)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_template_template_code ON notification_template (template_code);

CREATE TABLE IF NOT EXISTS notification_preference (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    consented_at TIMESTAMP(6),
    CONSTRAINT uk_notification_preference_user_type UNIQUE (user_id, notification_type)
);

CREATE INDEX IF NOT EXISTS idx_notification_preference_user_id ON notification_preference (user_id);

CREATE TABLE IF NOT EXISTS device_token (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    token VARCHAR(4096) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    device_id VARCHAR(200) NOT NULL,
    last_used_at TIMESTAMP(6) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_device_token_device_id ON device_token (device_id);
CREATE INDEX IF NOT EXISTS idx_device_token_user_id ON device_token (user_id);

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
