CREATE TABLE shipping_address_read_models
(
    id                     BIGSERIAL PRIMARY KEY,
    shipping_address_id    BIGINT       NOT NULL,
    user_id                BIGINT       NOT NULL,
    recipient_name         VARCHAR(31)  NOT NULL,
    recipient_phone_number VARCHAR(15)  NOT NULL,
    address                VARCHAR(255) NOT NULL,
    address_detail         VARCHAR(255) NOT NULL,
    status                 VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    modified_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shipping_address_read_model_shipping_address_id UNIQUE (shipping_address_id)
);

CREATE INDEX idx_shipping_address_read_model_user_id ON shipping_address_read_models (user_id);
