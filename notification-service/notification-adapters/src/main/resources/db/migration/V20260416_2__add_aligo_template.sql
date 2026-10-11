CREATE TABLE aligo_template (
    id                BIGSERIAL PRIMARY KEY,
    sender_key        VARCHAR(255) NOT NULL,
    template_code     VARCHAR(255) NOT NULL UNIQUE,
    template_name     VARCHAR(255) NOT NULL,
    template_content  TEXT         NOT NULL,
    template_title    VARCHAR(255),
    template_subtitle VARCHAR(255),
    template_type     VARCHAR(20)  NOT NULL,
    template_em_type  VARCHAR(20)  NOT NULL,
    template_secure   BOOLEAN      NOT NULL DEFAULT FALSE,
    template_advert   BOOLEAN      NOT NULL DEFAULT FALSE,
    image_url         VARCHAR(1000),
    image_name        VARCHAR(255),
    insp_status       VARCHAR(20)  NOT NULL DEFAULT 'REG',
    template_status   VARCHAR(20)  NOT NULL DEFAULT 'R',
    comments          TEXT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    modified_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE aligo_template_button (
    id                  BIGSERIAL PRIMARY KEY,
    aligo_template_id   BIGINT       NOT NULL REFERENCES aligo_template(id) ON DELETE CASCADE,
    ordering            INT          NOT NULL,
    name                VARCHAR(255) NOT NULL,
    link_type           VARCHAR(20)  NOT NULL,
    link_mo             VARCHAR(1000),
    link_pc             VARCHAR(1000),
    link_ios            VARCHAR(1000),
    link_and            VARCHAR(1000)
);

CREATE INDEX idx_aligo_template_sender_key ON aligo_template(sender_key);
CREATE INDEX idx_aligo_template_button_template_id ON aligo_template_button(aligo_template_id);
