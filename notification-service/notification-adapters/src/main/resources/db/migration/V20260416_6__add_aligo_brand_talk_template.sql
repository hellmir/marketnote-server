CREATE TABLE aligo_brand_talk_template (
    id BIGSERIAL PRIMARY KEY,
    sender_key VARCHAR(255) NOT NULL,
    template_code VARCHAR(255) NOT NULL UNIQUE,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    template_content TEXT NOT NULL,
    adult BOOLEAN NOT NULL DEFAULT FALSE,
    image_url VARCHAR(1024),
    image_link VARCHAR(1024),
    buttons_json TEXT,
    coupon_json TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    modified_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_aligo_brand_talk_template_sender_key ON aligo_brand_talk_template (sender_key);
