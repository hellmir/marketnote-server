CREATE TABLE notification_vendor_communication_history (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(31) NOT NULL,
    target_id VARCHAR(63),
    vendor_name VARCHAR(31) NOT NULL,
    communication_type VARCHAR(15) NOT NULL,
    sender VARCHAR(15) NOT NULL,
    exception TEXT,
    payload TEXT,
    payload_json JSONB,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_notification_vendor_communication_history_target
    ON notification_vendor_communication_history (target_type, target_id);

CREATE INDEX idx_notification_vendor_communication_history_created_at
    ON notification_vendor_communication_history (created_at);
