CREATE TABLE aligo_sender_profile (
    id              BIGSERIAL       PRIMARY KEY,
    sender_key      VARCHAR(100)    NOT NULL UNIQUE,
    plus_id         VARCHAR(100)    NOT NULL,
    phone_number    VARCHAR(20),
    category_code   VARCHAR(50),
    name            VARCHAR(200),
    license_num     VARCHAR(50),
    profile_inspection_status VARCHAR(10),
    sender_profile_status     VARCHAR(10),
    alim_use_yn     BOOLEAN         NOT NULL DEFAULT FALSE,
    uuid            VARCHAR(100),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    modified_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);
